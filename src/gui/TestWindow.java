package gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window.Type;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import miniChat.Client;
import miniChat.ServerChannel;

public class TestWindow {
	private JFrame frame;
	private JComboBox<ServerChannel> serverComboBox;
	private JComboBox<Client> clientComboBox;
	private final Action action = new ServerLaunchAction();
	private final Action action_1 = new ClientLaunchAction();
	
	
	public TestWindow() {
		initialize();
		
	}
	public void setVisible(boolean b) {
		frame.setVisible(b);
	}

	private void initialize() {
		frame = new JFrame();
		frame.setType(Type.UTILITY);
		frame.setResizable(false);
		frame.setTitle("Mini-Chat : Test");
		frame.setBounds(100, 100, 314, 237);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{697, 0};
		gridBagLayout.rowHeights = new int[]{103, 103, 103, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
		frame.getContentPane().setLayout(gridBagLayout);
		
		JButton btnLaunchNewServer = new JButton();
		btnLaunchNewServer.setAction(action);
		btnLaunchNewServer.setText("Launch new server");
		GridBagConstraints gbc_btnLaunchNewServer = new GridBagConstraints();
		gbc_btnLaunchNewServer.insets = new Insets(0, 0, 5, 0);
		gbc_btnLaunchNewServer.gridx = 0;
		gbc_btnLaunchNewServer.gridy = 0;
		frame.getContentPane().add(btnLaunchNewServer, gbc_btnLaunchNewServer);
		
		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 1;
		frame.getContentPane().add(panel, gbc_panel);
		
		serverComboBox = new JComboBox<ServerChannel>();
		panel.add(serverComboBox);
		
		clientComboBox = new JComboBox<Client>();
		panel.add(clientComboBox);
		
		JButton btnLaunchNewClient = new JButton();
		btnLaunchNewClient.setAction(action_1);
		btnLaunchNewClient.setText("Launch new client");
		GridBagConstraints gbc_btnLaunchNewClient = new GridBagConstraints();
		gbc_btnLaunchNewClient.insets = new Insets(0, 0, 5, 0);
		gbc_btnLaunchNewClient.gridx = 0;
		gbc_btnLaunchNewClient.gridy = 2;
		frame.getContentPane().add(btnLaunchNewClient, gbc_btnLaunchNewClient);
	}

	////////////////////// ACTIONS ///////////////////////
	private class ServerLaunchAction extends AbstractAction {
		private static final long serialVersionUID = -5476065456253460319L;
		public ServerLaunchAction() {
			putValue(NAME, "SwingAction");
			putValue(SHORT_DESCRIPTION, "Some short description");
		}
		public void actionPerformed(ActionEvent e) {
			ServerMainWindow servWindow = new ServerMainWindow();
			serverComboBox.addItem(servWindow.getServer());
			servWindow.setVisible(true);
		}
	}
	private class ClientLaunchAction extends AbstractAction {
		private static final long serialVersionUID = -1755281541557068974L;
		public ClientLaunchAction() {
			putValue(NAME, "SwingAction_1");
			putValue(SHORT_DESCRIPTION, "Some short description");
		}
		public void actionPerformed(ActionEvent e) {
			ServerChannel server = (ServerChannel) serverComboBox.getSelectedItem();
			ClientMainWindow clientWindow;
			try {
				clientWindow = new ClientMainWindow(server.getAddress());
				clientComboBox.addItem(clientWindow.getClient());
				clientWindow.setVisible(true);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
}
