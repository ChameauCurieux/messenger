package gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import miniChat.ServerChannel;
import javax.swing.ImageIcon;

public class ServerMainWindow {

	private JFrame frmMinichatServer;
	private static ServerChannel server;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {					
					ServerMainWindow window = new ServerMainWindow();
					window.frmMinichatServer.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 * @throws IOException 
	 */
	public ServerMainWindow() throws IOException {
		server = new ServerChannel();
		server.startServer();
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 * @throws IOException 
	 */
	private void initialize() throws IOException {
		frmMinichatServer = new JFrame();
		frmMinichatServer.setResizable(false);
		frmMinichatServer.setTitle("Mini-Chat Server");
		frmMinichatServer.setBounds(100, 100, 322, 220);
		frmMinichatServer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel addressPanel = new JPanel();
		addressPanel.setLayout(new GridLayout(0, 1, 0, 0));
		
		JLabel addressTipLabel = new JLabel("Server address :");
		addressTipLabel.setHorizontalAlignment(SwingConstants.CENTER);
		addressPanel.add(addressTipLabel);
		
		JLabel addressLabel = new JLabel(server.getAddress().toString());
		addressPanel.add(addressLabel);
		addressLabel.setHorizontalAlignment(SwingConstants.CENTER);
		
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new GridLayout(1, 1, 0, 0));
		
		JButton startServerButton = new JButton("Start Server");
		startServerButton.setIcon(new ImageIcon(ServerMainWindow.class.getResource("/gui/icons/start.png")));
		buttonsPanel.add(startServerButton, "cell 0 0,alignx left,aligny top");
		
		JButton stopServerButton = new JButton("Stop Server");
		stopServerButton.setIcon(new ImageIcon(ServerMainWindow.class.getResource("/gui/icons/stop.png")));
		buttonsPanel.add(stopServerButton, "cell 1 0,alignx left,aligny top");
		frmMinichatServer.getContentPane().setLayout(new BorderLayout(0, 0));
		frmMinichatServer.getContentPane().add(addressPanel, BorderLayout.CENTER);
		frmMinichatServer.getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
	}

}
