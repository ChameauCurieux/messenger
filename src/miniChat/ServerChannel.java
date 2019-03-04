package miniChat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;


public class ServerChannel {
	Selector selector;
	ServerSocketChannel serverChannel;
	Thread handler;
	boolean isRunning = false;
	String messageReceived;
	Map<SocketChannel, Set<byte[]>> waitingMessages;
	/* Use : waitingMessages[clientChannel][message] = hasBeenSent <=> the message has been sent to the client channel, if hasBeenSent = true
	 * When a message has been sent to all clients, its entries are deleted
	 */


	/**
	 * Creates a local server bound to the given port.
	 * It uses NIO channels to communicate with the clients.
	 * @param port : where the server accepts connections
	 */
	public ServerChannel(int port) {
		waitingMessages = new HashMap<SocketChannel, Set<byte[]>>();
		try {
			// create new selector
			selector = Selector.open();
			// create new channel
			serverChannel = ServerSocketChannel.open();
			// bind it to the port
			InetSocketAddress address = new InetSocketAddress("localhost", port);
			serverChannel.bind(address);
			// set it to non-blocking
			serverChannel.configureBlocking(false);
			// add it to the selector (interested in "accept" events)
			serverChannel.register(selector, serverChannel.validOps());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Launches the server handler
	 */
	public void startServer() {
		try {
			System.out.println(" Starting server " + serverChannel.getLocalAddress());
			System.out.println("-------------------------");
			isRunning = true;
			// creating new handler
			handler = new Thread(new ServerHandler());
		} catch (IOException e) {
			e.printStackTrace();
		}
		// start the handler
		handler.start();
	}

	/**
	 * Stops the server handler
	 * @throws InterruptedException 
	 */
	public void stopServer() throws InterruptedException {
		Thread.sleep(100);
		System.out.println("-------------------------");
		System.out.println(" Closing the server ...");
		isRunning = false;

		// Checking for unsent messages
		int unsent = 0;
		for (SocketChannel clientChannel : waitingMessages.keySet()) {
			Set<byte[]> entry = waitingMessages.get(clientChannel);
			for (byte[] message : entry) {
				try {
					String msg = new String(message);
					System.out.println("	> NOT sent to "
							+ clientChannel.getRemoteAddress()
							+ " : \""
							+ msg
							+ "\"");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("	Unsent messages : " + unsent);
	}

	/**
	 * Handles connection and I/O with clients in an infinite loop, until stopped, when it cleans up the remaining connections.
	 * Works in a separate thread.
	 * @author juju
	 *
	 */
	class ServerHandler implements Runnable {

		@Override
		public void run() {
			while(isRunning) {
				try {
					selector.selectNow();
					// checks whether the selector is open
					/*
					 * if (!selector.isOpen()) { break; }
					 */
					Set<SelectionKey> selectedKeys = selector.selectedKeys();
					Iterator<SelectionKey> iterator = selectedKeys.iterator();

					while (iterator.hasNext()) {
						SelectionKey key = iterator.next();
						iterator.remove();

						// Accepting
						if(key.isAcceptable()) {
							acceptConnection(key);
						}

						// Connecting
						if(key.isConnectable()) {
							SocketChannel clientChannel = (SocketChannel) key.channel();
							System.out.println(" + Finishing connection " + clientChannel.getRemoteAddress());
							clientChannel.finishConnect();
						}

						// Reading
						if(key.isValid() && key.isReadable()) {
							readMessage(key);
						}

						// Writing
						//if(!waitingMessages.isEmpty() && key.isValid() && key.isWritable()) {
						if (messageReceived != null && key.isValid() && key.isWritable()) {
							writeMessage(key);
						}
					}
				} catch (IOException | CancelledKeyException | ClosedSelectorException e) {
					e.printStackTrace();
				}
			}

			// end server : clean up
			try {
				for (SelectionKey key : selector.keys()) {
					SelectableChannel channel = key.channel();
					if (channel.equals(serverChannel)) {
						serverChannel.socket().close();
						System.out.println(" ... Closing server socket");
					}
					else {
						if (!key.isValid()) {
							continue;
						}
						SocketChannel clientChannel = (SocketChannel) channel;
						Socket clientSocket = clientChannel.socket();
						System.out.println(" ... Closing connection to client " + clientSocket.getRemoteSocketAddress());
						clientSocket.close();
					}
					key.cancel();
				}
				selector.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * Pass on the message that was received from one client, to all clients
		 * @param key : the destination of the message
		 */
		private void writeMessage(SelectionKey key) {
			SocketChannel clientChannel = (SocketChannel) key.channel();
			Set<byte[]> messageSet = waitingMessages.get(clientChannel);
			Iterator<byte[]> iterator = messageSet.iterator();
			if (iterator.hasNext()) {
				// we send the first awaiting message
				try {
					byte[] messageBytes = iterator.next();
					ByteBuffer buffer = ByteBuffer.wrap(messageBytes);
					clientChannel.write(buffer);
					iterator.remove();
					System.out.println("	>> To "+ clientChannel.getRemoteAddress() + " : \"" + new String(messageBytes) + "\"");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		/**
		 * Reads the message sent by one client
		 * @param key : the source of the message
		 */
		private void readMessage(SelectionKey key) {
			try {
				SocketChannel clientChannel = (SocketChannel) key.channel();
				ByteBuffer buffer = ByteBuffer.allocate(256);
				int nbBytesRead = clientChannel.read(buffer);

				// if connection closed client side => closing server side
				if (nbBytesRead == -1) {
					System.out.println(" - Lost connection to client " + clientChannel.getRemoteAddress());
					waitingMessages.remove(clientChannel);
					clientChannel.socket().close();
					key.cancel();
				}

				// else => reading message
				else {
					byte[] byteArray = buffer.array();
					messageReceived = new String(byteArray).trim();
					System.out.println("	<< From "+ clientChannel.getRemoteAddress() + " : \"" + messageReceived + "\"");

					/*
					 * HashMap<byte[], Boolean> map = waitingMessages.get(clientChannel);
					 * map.put(byteArray, true); waitingMessages.put(clientChannel, map);
					 */

					// adding it to the waiting list of all other clients, with hasBeenSent = false
					for (SocketChannel clientChan : waitingMessages.keySet()) {
						if (!clientChan.equals(clientChannel)) {
							System.out.println("	     -> " + clientChan.getRemoteAddress() + " waiting");
							Set<byte[]> messageSet = waitingMessages.get(clientChan);
							messageSet.add(byteArray);
							waitingMessages.put(clientChan, messageSet);
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * Accepts the connection requested by a client
		 * @param key : the client
		 */
		private void acceptConnection(SelectionKey key) {
			// creating new connection
			SocketChannel clientChannel;
			try {
				clientChannel = serverChannel.accept();
				clientChannel.configureBlocking(false);
				// adding it to the selector
				clientChannel.register(selector, clientChannel.validOps());
				// adding it to the hashmap
				waitingMessages.put(clientChannel, new LinkedHashSet<byte[]>());

				System.out.println(" + Added client " + clientChannel.getRemoteAddress());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}