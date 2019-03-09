package miniChat;

import java.awt.EventQueue;

import gui.ServerMainWindow;

public class TestMiniChat {

	public static void main(String[] args) {
		
		EventQueue.invokeLater(new Runnable() {
			ServerMainWindow serverWindow;
			
			public void run() {
				try {					
					serverWindow = new ServerMainWindow();
					serverWindow.frmMinichatServer.setVisible(true);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}