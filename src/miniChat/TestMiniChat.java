package miniChat;

public class TestMiniChat {
	static int port = 2000; // arbitrary value

	public static void main(String[] args) throws InterruptedException {
		ServerChannel server = new ServerChannel(port);
		server.startServer();
		Client client1 = new Client(port);
		Client client2 = new Client(port);

		// test messages client -> server
		client1.sendRandomMessages(1);
		client2.sendRandomMessages(1);
		client2.close();
		
		Client client3 = new Client(port);
		client3.sendRandomMessages(1);
		client1.sendRandomMessages(1);
		
		// close
		//client1.close();
		server.stopServer();
	}
}
