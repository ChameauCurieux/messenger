package miniChat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Random;

public class TestMiniChat {

	public static void main(String[] args) throws InterruptedException {
		int port = 2000; // arbitrary
		ServerChannel server = new ServerChannel(port);
		server.startServer();
		Client client1 = new Client(port);
		Client client2 = new Client(port);

		// test messages client -> server
		client1.sendRandomMessages(2);	
		client2.sendRandomMessages(2);
		client2.close();
		
		client1.sendRandomMessages(2);		

		// wait a bit
		Thread.sleep(1000);

		// close
		//client2.close();
		client1.close();
		server.stopServer();
	}	

	

	static class Client {
		static int instanceCount = 0;
		String name;
		SocketChannel clientChannel;
		Random generator;

		public Client(int port) {
			instanceCount++;
			name = "client" + instanceCount;
			generator = new Random();
			try {
				clientChannel = SocketChannel.open(new InetSocketAddress("localhost", port));
				clientChannel.configureBlocking(false);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public Client(int port, String n) {
			instanceCount++;
			name = n;
			generator = new Random();
			try {
				clientChannel = SocketChannel.open(new InetSocketAddress("localhost", port));
				clientChannel.configureBlocking(false);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
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
				ByteBuffer buffer = ByteBuffer.wrap(msg);
				try {
					clientChannel.write(buffer);
					Thread.sleep(10);
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		public void sendMessage(String msg) {
			try {
				byte[] message = msg.getBytes();
				ByteBuffer buffer = ByteBuffer.wrap(message);
				clientChannel.write(buffer);
				Thread.sleep(10);
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}

		public void close() {
			try {
				Socket clientSocket = clientChannel.socket();
				//System.out.println("(Client " + clientSocket.getLocalSocketAddress() + ") Left");
				clientSocket.close();
				clientChannel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
