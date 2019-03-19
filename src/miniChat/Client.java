package miniChat;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Random;

import gui.ClientMainWindow;
import utils.ArrayMethods;

public class Client implements AutoCloseable {
	public static int instanceCount = 0;
	// connection
	private SocketChannel clientChannel;
	// name
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
		this(ad, "client" + instanceCount);
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
		name = n;
		// for random messages
		generator = new Random();
		// connects to the server
		clientChannel = SocketChannel.open(ad);
		clientChannel.configureBlocking(false);
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
//				window.infoText.setText("Waiting for server...");
//				serverReady.wait();
//				window.infoText.setText("Connected to server");
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


	/**
	 * Links the window to the client and initialises the information
	 * @param clientMainWindow
	 */
	public void setWindow(ClientMainWindow clientMainWindow) {
		window = clientMainWindow;
		// updates window
		window.nameTextField.setText(name);
		try {
			window.addressTextField.setText(clientChannel.getLocalAddress().toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	 * Send the message msg to the server
	 * @param msg : the message to be sent
	 */
	public void sendMessage(String msg) {
		sendMessage(msg.getBytes());
	}

	/**
	 * Send the message to the server
	 * @param message : the message to be sent
	 */
	public void sendMessage(byte[] message) {
		try {
			if (message.length > 256) {
				// divide up the message in 256-bytes segments
				// TODO fuse segments in one long message
				List<byte[]> segments = ArrayMethods.split(message);
				for (byte[] segment : segments) {
					sendMessage(segment);
				}
			} else {
				ByteBuffer buffer = ByteBuffer.wrap(message);
				clientChannel.write(buffer);
				window.chatTextArea.append(name + " : > To server : \"" + new String(message) + "\"\n");
				Thread.sleep(10);
			}
		} catch (IOException | InterruptedException e) {
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
					ByteBuffer buffer = ByteBuffer.allocate(256);
					int nbBytesRead = clientChannel.read(buffer);

					// end-of-stream
					if (nbBytesRead == -1) {
						window.infoText.setText("Connexion lost");
						window.chatTextArea.append(name + " : Connexion to server lost\n");
						close();
					}
					// received message
					else if (nbBytesRead > 0) {
						// acknowledgement message
						if (!serverReady){
//							serverReady = true;
//							// notify the main thread
//							synchronized (serverReady) {
//								serverReady.notify();
//							}
						}
						// regular message
						else {
							window.chatTextArea.append(name + " : < From server : " + new String(buffer.array()) + "\n");							
						}
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
