package miniChat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TestMiniChat {

	public static void main(String[] args) {
		int port = 2000; // arbitrary
		Server server = new Server(port);
		server.startServer();
		Client client1 = new Client(port);
		Client client2 = new Client(port);
		
		// test messages client -> server
		client1.sendMessage("1");
		client1.sendMessage("2");
		
		// close up
		client1.close();
		client2.close();
		server.stopServer();
	}	


	static class Client {
		Socket socket;
		PrintWriter out;
		BufferedReader in;

		public Client(int port) {
			try {
				socket = new Socket("localhost", port);
				out = new PrintWriter(socket.getOutputStream());
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void sendMessage(String msg) {
			out.println(msg);
		}
		
		public void close() {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
