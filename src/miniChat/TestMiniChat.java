package miniChat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class TestMiniChat {

	public static void main(String[] args) throws InterruptedException {
		int port = 2000; // arbitrary
		ServerChannel server = new ServerChannel(port);
		server.startServer();
		Client client1 = new Client(port);
		Client client2 = new Client(port);

		// test messages client -> server
		client1.sendMessage("1");
		client1.sendMessage("-1");
		client2.sendMessage("2");
		client2.close();
		
		client1.sendMessage("--1");

		// wait a bit
		Thread.sleep(5000);
		
		// close
		//client2.close();
		client1.close();
		server.stopServer();
	}	


	static class Client {
		SocketChannel clientChannel;

		public Client(int port) {
			try {
				clientChannel = SocketChannel.open(new InetSocketAddress("localhost", port));
				clientChannel.configureBlocking(false);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void sendMessage(String msg) {
			// FIXME sending messages too close to one another (before the server reads) melds them together
			try {
				byte[] message = msg.getBytes();
				ByteBuffer buffer = ByteBuffer.wrap(message);
				clientChannel.write(buffer);
				Thread.sleep(5);
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
