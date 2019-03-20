package miniChat;

import java.io.IOException;

import javax.swing.SwingUtilities;

import gui.ClientMainWindow;
import gui.ServerMainWindow;

public class TestMiniChat {

	public static void main(String[] args) throws IOException {
		ServerChannel s = new ServerChannel();
		Client c = new Client(s.getAddress());
		Client c2 = new Client(s.getAddress());
		
		SwingUtilities.invokeLater(new Runnable() {
			
			public void run() {
				try {					
					ServerMainWindow serverWindow = new ServerMainWindow(s);
					serverWindow.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		SwingUtilities.invokeLater(new Runnable() {
			
			public void run() {
				try {
					ClientMainWindow clientWindow = new ClientMainWindow(c);
					clientWindow.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		SwingUtilities.invokeLater(new Runnable() {
			
			public void run() {
				try {
					ClientMainWindow clientWindow2 = new ClientMainWindow(c2);
					clientWindow2.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}