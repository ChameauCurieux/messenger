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
import javax.swing.JTextField;

public class TestWindow {
	private JFrame frame;
	// TODO focus on the selected window
	private JComboBox<ServerChannel> serverComboBox;
	private JComboBox<Client> clientComboBox;
	private final Action action = new ServerLaunchAction();
	private final Action action_1 = new ClientLaunchAction();
	private JButton btnLaunchNewClient;
	private JPanel clientPanel;
	private JTextField clientNameTextField;
	
	
	public TestWindow() {
		initialize();
		
	}
	public void setVisible(boolean b) {
		frame.setVisible(b);
	}

	private void initialize() {
		frame = new JFrame();
		frame.setType(Type.UTILITY);
		frame.setTitle("Mini-Chat : Test");
		frame.setBounds(100, 100, 469, 238);
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
		
		JPanel comboBoxesPanel = new JPanel();
		GridBagConstraints gbc_comboBoxesPanel = new GridBagConstraints();
		gbc_comboBoxesPanel.insets = new Insets(0, 0, 5, 0);
		gbc_comboBoxesPanel.gridx = 0;
		gbc_comboBoxesPanel.gridy = 1;
		frame.getContentPane().add(comboBoxesPanel, gbc_comboBoxesPanel);
		
		serverComboBox = new JComboBox<ServerChannel>();
		comboBoxesPanel.add(serverComboBox);
		
		clientComboBox = new JComboBox<Client>();
		comboBoxesPanel.add(clientComboBox);
		
		clientPanel = new JPanel();
		GridBagConstraints gbc_clientPanel = new GridBagConstraints();
		gbc_clientPanel.insets = new Insets(0, 0, 5, 0);
		gbc_clientPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_clientPanel.gridx = 0;
		gbc_clientPanel.gridy = 2;
		frame.getContentPane().add(clientPanel, gbc_clientPanel);
		
		clientNameTextField = new JTextField();
		clientNameTextField.setText("client name");
		clientNameTextField.setEnabled(false);
		clientPanel.add(clientNameTextField);
		clientNameTextField.setColumns(10);
		
		btnLaunchNewClient = new JButton();
		clientPanel.add(btnLaunchNewClient);
		btnLaunchNewClient.setAction(action_1);
		btnLaunchNewClient.setText("Launch new client");
		btnLaunchNewClient.setEnabled(false);
	}

	////////////////////// ACTIONS ///////////////////////
	private class ServerLaunchAction extends AbstractAction {
		private static final long serialVersionUID = -5476065456253460319L;
		public ServerLaunchAction() {
			putValue(NAME, "SwingAction");
			putValue(SHORT_DESCRIPTION, "Some short description");
		}
		public void actionPerformed(ActionEvent e) {
			// create new server window
			ServerMainWindow servWindow = new ServerMainWindow();
			serverComboBox.addItem(servWindow.getServer());
			servWindow.setVisible(true);
			// allow creating new clients
			btnLaunchNewClient.setEnabled(true);
			clientNameTextField.setEnabled(true);
			clientNameTextField.setText(null);
		}
	}
	private class ClientLaunchAction extends AbstractAction {
		private static final long serialVersionUID = -1755281541557068974L;
		public ClientLaunchAction() {
			putValue(NAME, "SwingAction_1");
			putValue(SHORT_DESCRIPTION, "Some short description");
		}
		public void actionPerformed(ActionEvent e) {
			// get current selected server
			ServerChannel server = (ServerChannel) serverComboBox.getSelectedItem();
			ClientMainWindow clientWindow;
			try {
				// create new client connected to this server
				String name = clientNameTextField.getText();
				clientWindow = new ClientMainWindow(server.getAddress(), name);
				clientComboBox.addItem(clientWindow.getClient());
				clientWindow.setVisible(true);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
}
