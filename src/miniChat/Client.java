package miniChat;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Random;

import gui.ClientMainWindow;
import utils.Messages;

public class Client implements AutoCloseable {
	public static int instanceCount = 0;
	// connection
	private SocketChannel clientChannel;
	private SocketAddress address;
	// name : cannot be longer than 255 characters
	private String name;
	// random
	private Random generator;
	// multi-threading
	private Thread messageListener; 
	private boolean isRunning = false;
	private Boolean serverReady = false;
	private Object doneRunning = new Object();
	private ClientMainWindow window;

	//////////////////////////////// CONSTRUCTORS ///////////////////////////////////
	/**
	 * Creates a client connected to the server at address ad.
	 * The name of the client is chosen generically.
	 * @param ad : server's address
	 * @throws IOException if opening the socket has failed in any way
	 */
	public Client(SocketAddress ad) throws IOException {
		this(ad, null);
	}

	/**
	 * Creates a client, named n, connected to the server at address ad.
	 * <br>Starts the client
	 * @param ad : server's address
	 * @param n : client's name
	 * @throws IOException if opening the socket has failed in any way
	 */
	public Client(SocketAddress ad, String n) throws IOException {
		instanceCount++;
		if (n == null || n.equals("")) {
			n = "client" + instanceCount;
		}
		name = n;
		// for random messages
		generator = new Random();
		// connects to the server
		clientChannel = SocketChannel.open(ad);
		clientChannel.configureBlocking(false);
		address = clientChannel.getLocalAddress();
	}

	//////////////////////////////// START & STOP ///////////////////////////////////
	/**
	 * Wait until server is ready, then
	 * sends own name to server &
	 * starts listening for messages
	 * @throws InterruptedException if the wait has been interrupted
	 */
	public void startClient() {
		// no effect if it is already running
		if (isRunning) {
			return;
		}
		// starts listening for messages
		isRunning = true;
		messageListener = new Thread(new MessageListener());
		messageListener.start();
		
		// wait until server is ready
//		synchronized (serverReady) {
//			try {
//				window.setInfo("Waiting for server...");
//				serverReady.wait();
//				window.setInfo("Connected to server");
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
		// send own name to server
		sendMessage(name);

	}

	/**
	 * Tells the messageListener to stop,
	 * and close the connection to the server
	 * after the reading is done
	 */
	public void close() {
		// no effect if it wasn't started
		if (!isRunning) {
			return;
		}
		
		isRunning = false;
		try {
			clientChannel.shutdownOutput();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// wait until all messages are read
//		synchronized (doneRunning) {
//			try {
//				doneRunning.wait();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}

		// then we close
		if (clientChannel.isOpen()) {
			try {
				clientChannel.socket().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public String toString() {
		String res;
		res = name + "(" + address + ")";
		return res;
	}
	
	/**
	 * Links the window to the client and initialises the information
	 * @param clientMainWindow
	 */
	public void setWindow(ClientMainWindow clientMainWindow) {
		window = clientMainWindow;
		// updates window
		window.setName(name);
		window.setAddress(address.toString());
	}
	
	//////////////////////////////// SENDING MESSAGES ///////////////////////////////////
	/**
	 * Sends a message of the given length,
	 * that consists of "123456789,12345..." and so on
	 * @param length
	 */
	public void sendTestMessage(int length) {
		byte[] message = new byte[length];
		int j = 1;
		for (int i = 0; i < length; i++) {
			if (j < 10) {
				message[i] = (byte) Integer.toString(j).charAt(0);
				j++;
			} else {
				message[i] = (byte) ",".charAt(0);
				j = 1;
			}
		}
		sendMessage(message);
	}

	/**
	 * Send messageNumber random 10-bytes-long messages to the server
	 * @param messageNumber : the number of messages to be sent
	 */
	public void sendRandomMessages(int messageNumber) {
		sendRandomMessages(messageNumber, 10);
	}

	public void sendRandomMessages(int messageNumber, int messageLength) {
		byte[] symbols = "abcedfghijklomnpqrstuvwxyzABCEDFGHIJKLMNOPQRSTUVWXYZ0123456789".getBytes();
		byte[] msg = new byte[messageLength];
		for (int j = 0; j < messageNumber; j++) {
			for (int i = 0; i < messageLength; i++) {
				msg[i] = symbols[generator.nextInt(symbols.length)];				
			}
			sendMessage(msg);
		}
	}

	/**
	 * Signs the message before sending it to the server.
	 * @param message
	 */
	public void sendSignedMessage(String message) {
		sendSignedMessage(message.getBytes());
	}
	
	/**
	 * Signs the message before sending it to the server.
	 * @param message
	 */
	public void sendSignedMessage(byte[] message) {
		byte[] signed = Messages.signMessage(name, message);
		sendMessage(signed);
	}

	/**
	 * Send the message msg to the server
	 * @param msg : the message to be sent
	 */
	public void sendMessage(String msg) {
		sendMessage(msg.getBytes());
	}

	/**
	 * Basic method : 
	 * Send the message to the server
	 * @param message : the message to be sent
	 */
	public void sendMessage(byte[] message) {
		try {
			if (message.length > Messages.bufferSize) {
				// divide up the message in buffer-sized segments
				List<byte[]> segments = Messages.split(message);
				for (byte[] segment : segments) {
					sendMessage(segment);
				}
			} else {
				ByteBuffer buffer = ByteBuffer.wrap(message);
				clientChannel.write(buffer);
				window.addMessage("> To server : \"" + new String(message) + "\"");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}	

	//////////////////////////////// HANDLER ///////////////////////////////////

	class MessageListener implements Runnable {

		@Override
		public void run() {
			// read indefinitely the server's message
			while (isRunning) {
				try {
					ByteBuffer buffer = ByteBuffer.allocate(Messages.bufferSize);
					int nbBytesRead = clientChannel.read(buffer);

					// end-of-stream
					if (nbBytesRead == -1) {
						window.setInfo("Connexion lost");
						window.addMessage("Connexion to server lost");
						window.setConnected(false);
						close();
					}
					
					// received message
					// TODO fuse long messages
					else if (nbBytesRead > 0) {
						// acknowledgement message
//						if (!serverReady){
//							serverReady = true;
//							// notify the main thread
//							synchronized (serverReady) {
//								serverReady.notify();
//							}
//						}
//						// regular message
//						else {
							byte[] message = Messages.trimmedArray(buffer, nbBytesRead);
							window.addMessage("< From " + Messages.toString(message));
//						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			// when reading is over, we tell the main thread
//			synchronized (doneRunning) {
//				doneRunning.notify();
//			}
		}

	}
}

/*
Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam at tincidunt nisl. Curabitur vitae mauris laoreet, tempus eros non, sollicitudin nunc. Praesent posuere sem libero, et congue tellus elementum at. Proin quis dui neque. Suspendisse scelerisque justo eget convallis finibus. Nullam diam turpis, porta quis tortor quis, ultricies interdum turpis. Nam eget lobortis risus. Nam suscipit efficitur odio a luctus. Aliquam tristique elementum vestibulum. Nullam sodales eget libero sit amet lacinia. Nulla ultricies odio justo, sit amet rhoncus ante malesuada a. Mauris ultrices risus nec pretium tristique. In convallis, enim ut vulputate elementum, massa dui dignissim libero, vulputate dignissim libero odio vitae magna.

Ut a rutrum felis, non suscipit nunc. Praesent justo dui, fringilla at orci in, porta viverra libero. Vivamus sed lobortis eros. Phasellus pellentesque, orci elementum dictum luctus, velit est pretium odio, eu sollicitudin lorem quam ut ligula. Integer ornare interdum diam, egestas ornare leo tempor vitae. Fusce a dapibus arcu. Aliquam vel iaculis orci.

Etiam tempor eu justo eget luctus. Nullam tincidunt augue volutpat nisi rutrum, id consequat turpis venenatis. Phasellus rutrum pretium mi, vel finibus dui laoreet sit amet. Vestibulum at augue et neque eleifend accumsan blandit sit amet enim. Quisque aliquet luctus felis ut pharetra. Integer eu leo iaculis, mattis felis ac, fermentum risus. Ut dignissim, diam et tristique tristique, sem nisl sodales nisl, sit amet rhoncus ligula quam nec libero. Donec porttitor nulla nisi, id rutrum sem porttitor sed. Sed metus purus, ullamcorper sed sem vel, iaculis viverra sem. Mauris massa lorem, imperdiet sit amet maximus facilisis, vulputate non velit.

Vivamus sed nisi eu mauris rutrum elementum. Nam interdum, neque et cursus sagittis, diam odio egestas libero, sed cursus neque dolor nec nisl. Integer convallis venenatis eros, fringilla congue quam suscipit sit amet. Vestibulum at risus quis ex dignissim varius vitae non dui. Vestibulum id massa eu sapien sollicitudin ultricies. Nulla quis consequat ex. Cras ut vehicula magna, eget cursus diam. Nam elementum congue fermentum. Aenean laoreet tempor tortor, sit amet mollis augue euismod a. In tincidunt metus lectus. Duis ut hendrerit lorem, nec porta ipsum. Duis malesuada, ex et finibus consectetur, elit arcu sagittis nisl, in luctus purus mi non nunc. Nulla sodales quis odio ac finibus. Aenean eget urna dictum, imperdiet dui quis, posuere arcu. Etiam venenatis velit ut dolor euismod, ut gravida metus cursus.

Duis elementum sem fringilla neque blandit posuere. Ut et accumsan urna, in aliquam nibh. Proin neque quam, mattis sit amet turpis a, commodo lobortis purus. Maecenas pellentesque porta felis a mattis. Etiam volutpat risus ac vestibulum dignissim. Ut scelerisque nisl sit amet erat volutpat, eu vehicula lorem fermentum. Nulla dapibus turpis sit amet est porttitor luctus. 

 */