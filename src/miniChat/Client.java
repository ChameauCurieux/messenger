package miniChat;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Random;

public class Client implements AutoCloseable {
	static int instanceCount = 0;
	String name;
	SocketChannel clientChannel;
	Thread messageListener; 
	Random generator;
	boolean isRunning;

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

	public void sendMessage(String msg) {
		sendMessage(msg.getBytes());
	}
	
	public void sendMessage(byte[] message) {
		try {
			ByteBuffer buffer = ByteBuffer.wrap(message);
			clientChannel.write(buffer);
			System.err.println(name + " : > To server : \"" + new String(message) + "\"");
			Thread.sleep(10);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		
	}

	public void startClient() {
		// send own name to server
		sendMessage(name);
		// starts listening for messages
		isRunning = true;
		messageListener = new Thread(new MessageListener());
		messageListener.start();

	}

	public void close() {
		isRunning = false;
		if (clientChannel.isOpen()) {
			try {
				Socket clientSocket = clientChannel.socket();
				//System.err.println("(Client " + clientSocket.getLocalSocketAddress() + ") Leaving");
				clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	class MessageListener implements Runnable {

		@Override
		public void run() {
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
		}

	}
}
