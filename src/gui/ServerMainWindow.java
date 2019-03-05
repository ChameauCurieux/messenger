package gui;

import java.awt.EventQueue;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import miniChat.ServerChannel;
import miniChat.TestMiniChat;
import net.miginfocom.swing.MigLayout;

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
					server = new ServerChannel(TestMiniChat.port);
					server.startServer();
					
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
	 */
	public ServerMainWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmMinichatServer = new JFrame();
		frmMinichatServer.setTitle("Mini-Chat Server");
		frmMinichatServer.setBounds(100, 100, 333, 243);
		frmMinichatServer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel addressPanel = new JPanel();
		addressPanel.setLayout(new GridLayout(0, 1, 0, 0));
		
		JLabel addressTipLabel = new JLabel("Server address :");
		addressTipLabel.setHorizontalAlignment(SwingConstants.CENTER);
		addressPanel.add(addressTipLabel);
		
		JLabel addressLabel = new JLabel(TestMiniChat.serverSocketAddress.toString());
		addressPanel.add(addressLabel);
		addressLabel.setHorizontalAlignment(SwingConstants.CENTER);
		
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new MigLayout("", "[93px][91px]", "[23px]"));
		
		JButton startServerButton = new JButton("Start Server");
		buttonsPanel.add(startServerButton, "cell 0 0,alignx left,aligny top");
		
		JButton stopServerButton = new JButton("Stop Server");
		buttonsPanel.add(stopServerButton, "cell 1 0,alignx left,aligny top");
		frmMinichatServer.getContentPane().setLayout(new MigLayout("", "[442px]", "[136px][136px]"));
		frmMinichatServer.getContentPane().add(addressPanel, "cell 0 0,grow");
		frmMinichatServer.getContentPane().add(buttonsPanel, "cell 0 1,alignx center,aligny center");
	}

}
