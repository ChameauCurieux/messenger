package miniChat;

import java.io.IOException;
import java.net.SocketAddress;

public class TestMiniChat {

	public static void main(String[] args) throws InterruptedException, IOException {
		ServerChannel server = new ServerChannel();
		SocketAddress address = server.getAddress();
		server.startServer();
		try (Client client1 = new Client(address, "Bonnie");
			Client client2 = new Client(address, "Clyde")){
			client1.sendMessage("a");
			client2.sendTestMessage(257);
		}
		
		// close
		server.stopServer();
	}
}