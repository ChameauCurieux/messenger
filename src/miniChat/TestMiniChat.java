package miniChat;

import java.awt.EventQueue;
import java.io.IOException;

import gui.ServerMainWindow;

public class TestMiniChat {

	public static void main(String[] args) {
		ServerChannel server = new ServerChannel();
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try (Client client = new Client(server.getAddress());) {					
					ServerMainWindow.instance = new ServerMainWindow(server);
					ServerMainWindow.frmMinichatServer.setVisible(true);
					
					client.sendMessage("hello");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}