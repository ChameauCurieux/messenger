package miniChat;

import java.io.IOException;

public class TestMiniChat {

	public static void main(String[] args) throws InterruptedException {
		ServerChannel server = new ServerChannel();
		server.startServer();
		try (Client client1 = new Client(server.getAddress());
			Client client2 = new Client(server.getAddress())){
			client1.sendMessage("hello");
			client2.sendMessage("hi");
			Thread.sleep(1000);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// close
		server.stopServer();
	}
}
