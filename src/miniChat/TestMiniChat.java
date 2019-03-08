package miniChat;

import java.io.IOException;

public class TestMiniChat {

	public static void main(String[] args) throws InterruptedException {
		ServerChannel server = new ServerChannel();
		server.startServer();
		try (Client client1 = new Client(server.getAddress());
			Client client2 = new Client(server.getAddress())){
			client1.sendMessage("hello");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// close
		server.stopServer();
	}
}
