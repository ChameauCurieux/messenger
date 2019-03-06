package miniChat;

import java.io.IOException;

public class TestMiniChat {

	public static void main(String[] args) throws InterruptedException {
		ServerChannel server = new ServerChannel();
		server.startServer();
		Client client1;
		Client client2;
		try {
			client1 = new Client(server.getAddress());
			client2 = new Client(server.getAddress());
			// test messages client -> server
			client1.sendRandomMessages(1);
			client2.sendRandomMessages(1);
			// minus one client
			client2.close();
			// plus one client
			Client client3 = new Client(server.getAddress());
			client3.sendRandomMessages(1);
			client1.sendRandomMessages(1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// close
		//client1.close();
		server.stopServer();
	}
}
