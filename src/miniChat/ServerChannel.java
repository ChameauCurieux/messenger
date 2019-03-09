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

import gui.ServerMainWindow;
import utils.ArrayMethods;


public class ServerChannel implements AutoCloseable {
	// connecting to the web
	Selector selector;
	ServerSocketChannel serverChannel;
	Integer port;

	// managing client messages 
	int nbClientsConnected = 0;
	String messageReceived;
	Map<SocketChannel, Set<byte[]>> waitingMessages;
	Map<SocketChannel, String> clientNames;

	// multi-threading
	Thread serverHandler;
	/**
	 * True when the handler is running, <br>false otherwise
	 */
	boolean isRunning;
	/**
	 * True when the handler has stopped, but before it was restarted
	 * <br>False at server creation and after handler start
	 */
	Boolean doneRunning;

	// gui
	ServerMainWindow window;

	///////////////////////////////// CONSTRUCTORS /////////////////////////////////
	/**
	 * Creates a local server bound to the given port.
	 * It uses NIO channels to communicate with the clients.
	 * @param window 
	 * @param port : where the server accepts connections
	 */
	public ServerChannel(int p) {
		isRunning = false;
		doneRunning = false;
		waitingMessages = new HashMap<SocketChannel, Set<byte[]>>();
		clientNames = new HashMap<SocketChannel, String>();
		port = p;
	}

	/**
	 * Creates a local server bound to a random port.
	 * It uses NIO channels to communicate with the clients.
	 */
	public ServerChannel() {
		this(0);
	}

	/**
	 * Creates a local server bound to the given port.
	 * It is linked to the given window.
	 * It uses NIO channels to communicate with the clients.
	 * @param w : used for display
	 * @param port : where the server accepts connections
	 */
	public ServerChannel(ServerMainWindow w, int p) {
		this(p);
		window = w;
	}

	/**
	 * Creates a local server bound to a random port.
	 * It is linked to the given window.
	 * It uses NIO channels to communicate with the clients.
	 * @param window : used for display
	 */
	public ServerChannel(ServerMainWindow window) {
		this(window, 0);
	}

	///////////////////////////////// START & STOP /////////////////////////////////
	/**
	 * Starts the selector, socketChannel and handler
	 */
	public void startServer() {
		// no effect if already started
		if (isRunning) {
			return;
		}

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

			// updates the window
			window.addressTextField.setText(address.toString());
			window.infoText.setText("Starting server " + serverChannel.getLocalAddress());
			window.chatTextArea.append("server : Starting server " + serverChannel.getLocalAddress() + "\n");
			isRunning = true;
			doneRunning = false;
			
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
	public void close(){
		// no effect if server not started
		if (!isRunning) {
			return;
		}

		isRunning = false;

		// wait until the handler has processed all messages
		if (!doneRunning) {
			synchronized (doneRunning) {
				try {
					doneRunning.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		// update window
		window.infoText.setText("Closing the server ...\n");
		window.chatTextArea.append("server : Closing the server ...\n");

		// Checking for unsent messages
		int unsent = 0;
		for (SocketChannel clientChannel : waitingMessages.keySet()) {
			Set<byte[]> entry = waitingMessages.get(clientChannel);
			for (byte[] message : entry) {
				String msg = new String(message);
				window.chatTextArea.append("server : 	> NOT sent to "
						+ clientNames.get(clientChannel)
						+ " : \""
						+ msg
						+ "\"\n");
			}
		}
		window.chatTextArea.append("server : 	... Unsent messages : " + unsent + "\n");

		// end server : clean up
		try {
			for (SelectionKey key : selector.keys()) {
				// ignoring keys that were previously closed
				if (!key.isValid()) {
					continue;
				}

				SelectableChannel channel = key.channel();
				if (channel.equals(serverChannel)) {
					window.chatTextArea.append("server :	... Closing server socket\n");
					((ServerSocketChannel)channel).socket().close();
					key.cancel();
				}
				else {
					window.chatTextArea.append("server :	... Closing connection to client " + ((SocketChannel) channel).getRemoteAddress());
					closeClient(key);
				}
			}
			window.chatTextArea.append("server :	... Closing server selector\n");
			selector.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		window.infoText.setText("Goodbye\n");
		window.chatTextArea.append("server : Done.\n");
		window.chatTextArea.append("================================\n");
	}

	/**
	 * Properly closes the connection to the client
	 * @param key : a SelectionKey (associated to a client)
	 * @throws IOException
	 */
	private void closeClient(SelectionKey key) throws IOException {
		nbClientsConnected--;
		SocketChannel clientChannel = (SocketChannel) key.channel();
		clientChannel.socket().close();
		key.cancel();
	}

	///////////////////////////////// ACCESSERS /////////////////////////////////

	public SocketAddress getAddress() throws IOException {
		return serverChannel.getLocalAddress();
	}

	public int getPort() {
		return port;
	}

	public boolean wasStartedAndClosed() {
		return doneRunning;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void setWindow(ServerMainWindow w) {
		window = w;
	}



	///////////////////////////////// HANDLER /////////////////////////////////
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
							window.chatTextArea.append("server : + Finishing connection " + clientNames.get(clientChannel));
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

			// running is over => notify main thread
			synchronized (doneRunning) {
				doneRunning.notifyAll();
			}
			doneRunning = true;

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
					window.chatTextArea.append("server : > To "+ clientNames.get(clientChannel) + " : \"" + new String(messageBytes) + "\"\n");
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
					window.infoText.setText("Connection lost with " + clientNames.get(clientChannel));
					window.chatTextArea.append("server : - Lost connection to client " + clientNames.get(clientChannel) + "\n");
					closeClient(key);
				}

				// else => reading message
				else {
					byte[] byteArray = ArrayMethods.trimArray(buffer, nbBytesRead);
					messageReceived = new String(byteArray);

					// first message received = client name
					if (clientNames.get(clientChannel) == null) {
						messageReceived = messageReceived.trim();
						clientNames.put(clientChannel, messageReceived);
						window.infoText.setText("New client : " + messageReceived);
						window.chatTextArea.append("server :   Named client " + clientChannel.getRemoteAddress() + " -> \"" + messageReceived + "\"\n");
						window.clientListLabel.setText("Connected (" + nbClientsConnected + "):\n\n");
						window.clientListTextArea.append(messageReceived + " (" + clientChannel.getRemoteAddress() + ")\n");
					}
					// next messages = to be transmitted
					else {

						window.chatTextArea.append("server : < From "+ clientNames.get(clientChannel) + " : \"" + messageReceived + "\"\n");

						// adding it to the waiting list of all other clients, with hasBeenSent = false
						for (SocketChannel otherClientChan : waitingMessages.keySet()) {
							if (!otherClientChan.equals(clientChannel)) {
								window.chatTextArea.append("server :	Waiting -> " + clientNames.get(otherClientChan));
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
				nbClientsConnected++;
				window.chatTextArea.append("server : + Added client " + clientChannel.getRemoteAddress() + "\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}