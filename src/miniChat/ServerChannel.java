package miniChat;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
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

import utils.ArrayMethods;


public class ServerChannel {
	// connecting to the web
	Selector selector;
	ServerSocketChannel serverChannel;

	// managing client messages 
	String messageReceived;
	Map<SocketChannel, Set<byte[]>> waitingMessages;
	Map<SocketChannel, String> clientNames;

	// multi-threading
	Thread serverHandler;
	boolean isRunning = false;
	Object doneRunning = new Object();


	/**
	 * Creates a local server bound to the given port.
	 * It uses NIO channels to communicate with the clients.
	 * @param port : where the server accepts connections
	 */
	public ServerChannel(int port) {
		waitingMessages = new HashMap<SocketChannel, Set<byte[]>>();
		clientNames = new HashMap<SocketChannel, String>();
		try {
			// create new selector
			selector = Selector.open();
			// create new channel
			serverChannel = ServerSocketChannel.open();
			// bind it to the port
			InetSocketAddress address = new InetSocketAddress(InetAddress.getLocalHost(), port);
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
	 * Creates a local server bound to a random port.
	 * It uses NIO channels to communicate with the clients.
	 */
	public ServerChannel() {
		this(0);
	}

	/**
	 * Launches the server handler
	 */
	public void startServer() {
		try {
			System.out.println("server : Starting server " + serverChannel.getLocalAddress());
			System.out.println("server : -------------------------");
			isRunning = true;
			// creating new handler
			serverHandler = new Thread(new ServerHandler());
		} catch (IOException e) {
			e.printStackTrace();
		}
		// start the handler
		serverHandler.start();
	}

	/**
	 * Stops the server handler
	 * @throws InterruptedException 
	 */
	public void stopServer(){
		// no effect if server not started
		if (!isRunning) {
			return;
		}

		isRunning = false;

		// wait until the handler has processed all messages
		synchronized (doneRunning) {
			try {
				doneRunning.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("server : -------------------------");
		System.out.println("server : Closing the server ...");

		// Checking for unsent messages
		int unsent = 0;
		for (SocketChannel clientChannel : waitingMessages.keySet()) {
			Set<byte[]> entry = waitingMessages.get(clientChannel);
			for (byte[] message : entry) {
				String msg = new String(message);
				System.out.println("server : 	> NOT sent to "
						+ clientNames.get(clientChannel)
						+ " : \""
						+ msg
						+ "\"");
			}
		}
		System.out.println("server : 	Unsent messages : " + unsent);

		// end server : clean up
		try {
			for (SelectionKey key : selector.keys()) {
				// ignoring keys that were previously closed
				if (!key.isValid()) {
					continue;
				}

				SelectableChannel channel = key.channel();
				if (channel.equals(serverChannel)) {
					System.out.println("server :	... Closing server socket");
					((ServerSocketChannel)channel).socket().close();
					key.cancel();
				}
				else {
					System.out.println("server :	... Closing connection to client " + ((SocketChannel) channel).getRemoteAddress());
					closeClient(key);
				}
			}
			System.out.println("server :	... Closing server selector");
			selector.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("server : Done.");
	}

	public SocketAddress getAddress() throws IOException {
		return serverChannel.getLocalAddress();
	}

	/**
	 * Properly closes the connection to the client
	 * @param key : a SelectionKey (associated to a client)
	 * @throws IOException
	 */
	private void closeClient(SelectionKey key) throws IOException {
		SocketChannel clientChannel = (SocketChannel) key.channel();
		clientChannel.socket().close();
		key.cancel();
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
							System.out.println("server : + Finishing connection " + clientNames.get(clientChannel));
							clientChannel.finishConnect();
						}

						// Reading
						if(key.isValid() && key.isReadable()) {
							readMessage(key);
						}

						// Writing
						if (messageReceived != null && key.isValid() && key.isWritable()) {
							writeMessage(key);
						}
					}
				} catch (IOException | CancelledKeyException | ClosedSelectorException e) {
					e.printStackTrace();
				}
			}

			synchronized (doneRunning) {
				doneRunning.notify();
			}

		}

		/**
		 * Pass on one message that was received from one client, to another client
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
					System.out.println("server : > To "+ clientNames.get(clientChannel) + " : \"" + new String(messageBytes) + "\"");
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

				// if end of connection client side => closing
				if (nbBytesRead == -1) {
					System.out.println("server : - Lost connection to client " + clientNames.get(clientChannel));
					closeClient(key);
				}

				// else => reading message
				else {
					byte[] byteArray = ArrayMethods.trimArray(buffer, nbBytesRead);
					messageReceived = new String(byteArray);
					
					// first message received = client name
					if (clientNames.get(clientChannel) == null) {
						messageReceived = messageReceived.trim();
						System.out.println("server :   Named client " + clientChannel.getRemoteAddress() + " -> \"" + messageReceived + "\"");
						clientNames.put(clientChannel, messageReceived);
					}
					// next messages = to be transmitted
					else {

						System.out.println("server : < From "+ clientNames.get(clientChannel) + " : \"" + messageReceived + "\"");

						// adding it to the waiting list of all other clients, with hasBeenSent = false
						for (SocketChannel otherClientChan : waitingMessages.keySet()) {
							if (!otherClientChan.equals(clientChannel)) {
								System.out.println("server :	Waiting -> " + clientNames.get(otherClientChan));
								Set<byte[]> messageSet = waitingMessages.get(otherClientChan);
								messageSet.add(byteArray);
								waitingMessages.put(otherClientChan, messageSet);
							}
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
				System.out.println("server : + Added client " + clientChannel.getRemoteAddress());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}