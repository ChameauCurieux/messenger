package miniChat;

import java.awt.EventQueue;

import gui.ClientMainWindow;
import gui.ServerMainWindow;

public class TestMiniChat {

	public static void main(String[] args) {
		
		EventQueue.invokeLater(new Runnable() {
			
			public void run() {
				try {					
					ServerMainWindow serverWindow = new ServerMainWindow();
					serverWindow.frame.setVisible(true);
					ServerChannel server = serverWindow.getServer();
					server.startServer();
					
					ClientMainWindow clientWindow = new ClientMainWindow(server.getAddress());
					clientWindow.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}