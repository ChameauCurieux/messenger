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
import java.util.Iterator;
import java.util.Set;


public class ServerChannel {
	Selector selector;
	ServerSocketChannel serverChannel;
	Thread handler;
	boolean isRunning = false;
	//List<String> waitingMessages; TODO keep a list of messages to send
	String messageReceived;

	/**
	 * Creates a local server bound to the given port.
	 * It uses NIO channels to communicate with the clients.
	 * @param port : where the server accepts connections
	 */
	public ServerChannel(int port) {
		//waitingMessages = new ArrayList<String>();
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
	 * Launch the server handler
	 */
	public void startServer() {
		try {
			System.out.println("Starting server " + serverChannel.getLocalAddress());
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
	 * Stop the server handler and close remaining connections
	 */
	public void stopServer() {
		System.out.println("Closing the server :");
		isRunning = false;
		try {
			for (SelectionKey key : selector.keys()) {
				SelectableChannel channel = key.channel();
				if (channel.equals(serverChannel)) {
					serverChannel.socket().close();			
					serverChannel.close();
					System.out.println("	- Closing server socket");
				}
				else {
					SocketChannel clientChannel = (SocketChannel) channel;
					Socket clientSocket = clientChannel.socket();
					System.out.println("	- Closing connection to client " + clientSocket.getRemoteSocketAddress());
					clientSocket.close();
					clientChannel.close();
				}
				key.cancel();
			}
			selector.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handles connection and I/O with clients in an infinite loop, until stopped.
	 * Works in a separate thread.
	 * @author juju
	 *
	 */
	class ServerHandler implements Runnable {

		@Override
		public void run() {
			while(isRunning) {
				try {
					selector.select();
					// checks whether the selector is open
					if (!selector.isOpen()) {
						break;
					}
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
							System.out.println("+ Finishing connection " + clientChannel.getRemoteAddress());
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
		}

		/**
		 * Pass on the message that was received from one client, to all clients
		 * @param key : the destination of the message
		 */
		private void writeMessage(SelectionKey key) {
			/*
			 * TODO send the message ONCE to all clients
			 * SocketChannel clientChannel = (SocketChannel) key.channel(); try { byte[]
			 * messageBytes = messageReceived.getBytes(); ByteBuffer buffer =
			 * ByteBuffer.wrap(messageBytes); clientChannel.write(buffer); Thread.sleep(10);
			 * } catch (IOException | InterruptedException e) { e.printStackTrace(); }
			 * System.out.println(">> "+ clientChannel.getRemoteAddress() + " : \"" + messageReceived + "\"");
			 */
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
					System.out.println("- Lost connection with client " + clientChannel.getRemoteAddress());
					clientChannel.socket().close();
					clientChannel.close();
					key.cancel();
					return;
				}
				// else => reading message
				byte[] byteArray = buffer.array();
				messageReceived = new String(byteArray).trim();
				System.out.println("<< "+ clientChannel.getRemoteAddress() + " : \"" + messageReceived + "\"");
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
				System.out.println("+ Added client " + clientChannel.getRemoteAddress());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}