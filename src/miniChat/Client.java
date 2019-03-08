package miniChat;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Random;

import utils.ArrayMethods;

public class Client implements AutoCloseable {
	static int instanceCount = 0;
	// connection
	SocketChannel clientChannel;
	// name
	String name;
	// random
	Random generator;
	// multi-threading
	Thread messageListener; 
	boolean isRunning = false;
	Object doneRunning = new Object();

	///////////////////// CONSTRUCTORS ////////////////////////
	/**
	 * Creates a client connected to the server at address ad.
	 * The name of the client is chosen generically.
	 * @param ad : server's address
	 */
	public Client(SocketAddress ad) {
		this(ad, "client" + instanceCount);
	}

	/**
	 * Creates a client, named n, connected to the server at address ad
	 * @param ad : server's address
	 * @param n : client's name
	 */
	public Client(SocketAddress address, String n) {
		instanceCount++;
		name = n;
		// for random messages
		generator = new Random();
		try {
			// connects to the server
			clientChannel = SocketChannel.open(address);
			clientChannel.configureBlocking(false);
			startClient();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	///////////////////// SENDING MESSAGES ////////////////////////
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
				List<byte[]> segments = ArrayMethods.split(message);
				for (byte[] segment : segments) {
					sendMessage(segment);
				}
			} else {
			ByteBuffer buffer = ByteBuffer.wrap(message);
			clientChannel.write(buffer);
			System.err.println(name + " : > To server : \"" + new String(message) + "\"");
			Thread.sleep(10);
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	

	///////////////////// START & STOP ////////////////////////
	/**
	 * Sends own name to server &
	 * Starts listening to the server's messages
	 */
	public void startClient() {
		// send own name to server
		sendMessage(name);
		// starts listening for messages
		isRunning = true;
		messageListener = new Thread(new MessageListener());
		messageListener.start();

	}

	/**
	 * Tells the messageListener to stop,
	 * and close the connection to the server
	 * after the reading is done
	 */
	public void close() {
		isRunning = false;
		try {
			clientChannel.shutdownOutput();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		// wait until all messages are read
		synchronized (doneRunning) {
			try {
				doneRunning.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		// then we close
		if (clientChannel.isOpen()) {
			try {
				clientChannel.socket().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	class MessageListener implements Runnable {

		@Override
		public void run() {
			// read indefinitely the server's message
			while (isRunning) {
				try {
					ByteBuffer buffer = ByteBuffer.allocate(256);
					int nbBytesRead = clientChannel.read(buffer);

					if (nbBytesRead == -1) {
						System.err.println(name + " : Connexion to server lost");
						close();
					}
					else if (nbBytesRead > 0){
						System.err.println(name + " : < From server : " + new String(buffer.array()));
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			// when reading is over, we tell the main thread
			synchronized (doneRunning) {
				doneRunning.notify();
			}
		}

	}
}
