package gui;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DropMode;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import miniChat.ServerChannel;

/**
 * Displays informations and controls for a server.
 * Its content is updated by the associated server.
 * @author juju
 *
 */
public class ServerMainWindow extends MainWindow {

	private JTextArea chatInputTextArea;
	private JTextArea clientListTextArea;
	private JLabel clientListLabel;
	private ServerChannel server;

	private final Action startServerAction = new StartAction();
	private final Action stopServerAction = new StopAction();
	private JButton startServerButton;
	private JButton stopServerButton;

	/**
	 * Launch the application.
	 */
//	public static void main(String[] args) {
//		EventQueue.invokeLater(new Runnable() {
//			public void run() {
//				try {					
//					ServerMainWindow.instance = new ServerMainWindow();
//					ServerMainWindow.frmMinichatServer.setVisible(true);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
//	}

	/**
	 * Create the application with a new Server
	 * @wbp.parser.constructor 
	 */
	public ServerMainWindow() {
		this(new ServerChannel());
	}
	
	/**
	 * Create the application, linked to an existing Server
	 */
	public ServerMainWindow(ServerChannel serv) {
		server = serv;
		initialize();
		server.setWindow(this);
	}
	
	public ServerChannel getServer() {
		return server;
	}

	public void setServer(ServerChannel server) {
		this.server = server;
	}

	/**
	 * Initialize the contents of the frame.
	 * @throws IOException 
	 */
	private void initialize() {
		
		frame = new JFrame();
		frame.setResizable(false);
		frame.setTitle("Mini-Chat : Server");
		frame.setBounds(100, 100, 713, 347);
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new ServerWindowCloser());
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowWeights = new double[]{1.0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0};
		gridBagLayout.columnWidths = new int[]{0, 0};
		frame.getContentPane().setLayout(gridBagLayout);

		chatTextArea = new JTextArea();
		chatTextArea.setToolTipText("message history");
		chatTextArea.setEditable(false);
		chatTextArea.setFont(new Font("Consolas", Font.PLAIN, 12));
		chatTextArea.setDropMode(DropMode.INSERT);
		chatTextArea.setTabSize(4);
		chatTextArea.setWrapStyleWord(true);
		chatTextArea.setLineWrap(true);

		JScrollPane chatTextScrollPane = new JScrollPane(chatTextArea);

		chatInputTextArea = new JTextArea();
		chatInputTextArea.setEnabled(false);
		chatInputTextArea.setToolTipText("(doesn't do anything yet)");
		chatInputTextArea.setWrapStyleWord(true);
		chatInputTextArea.setLineWrap(true);
		chatInputTextArea.setTabSize(4);

		chatInputScrollPane = new JScrollPane(chatInputTextArea);


		JPanel commandPanel = new JPanel();
		GridBagConstraints gbc_commandPanel = new GridBagConstraints();
		gbc_commandPanel.anchor = GridBagConstraints.WEST;
		gbc_commandPanel.insets = new Insets(0, 0, 5, 5);
		gbc_commandPanel.gridx = 0;
		gbc_commandPanel.gridy = 0;
		frame.getContentPane().add(commandPanel, gbc_commandPanel);
		GridBagLayout gbl_commandPanel = new GridBagLayout();
		gbl_commandPanel.columnWidths = new int[]{282, 0};
		gbl_commandPanel.rowHeights = new int[]{38, 0, 0, 0, 0};
		gbl_commandPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_commandPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		commandPanel.setLayout(gbl_commandPanel);

		JPanel addressPanel = new JPanel();
		addressPanel.setToolTipText("where the clients need to connect");
		GridBagConstraints gbc_addressPanel = new GridBagConstraints();
		gbc_addressPanel.insets = new Insets(0, 0, 5, 0);
		gbc_addressPanel.gridx = 0;
		gbc_addressPanel.gridy = 0;
		commandPanel.add(addressPanel, gbc_addressPanel);
		addressPanel.setLayout(new GridLayout(0, 1, 0, 0));

		JLabel addressTipLabel = new JLabel("Server address :");
		addressTipLabel.setVerticalAlignment(SwingConstants.BOTTOM);
		addressTipLabel.setHorizontalAlignment(SwingConstants.CENTER);
		addressPanel.add(addressTipLabel);

		addressTextField = new JTextField();
		addressTipLabel.setLabelFor(addressTextField);
		addressTextField.setEditable(false);
		addressPanel.add(addressTextField);
		addressTextField.setHorizontalAlignment(SwingConstants.CENTER);

		JPanel clientListPanel = new JPanel();
		clientListPanel.setToolTipText("");
		GridBagConstraints gbc_clientListPanel = new GridBagConstraints();
		gbc_clientListPanel.insets = new Insets(0, 0, 5, 0);
		gbc_clientListPanel.gridx = 0;
		gbc_clientListPanel.gridy = 1;
		commandPanel.add(clientListPanel, gbc_clientListPanel);
		GridBagLayout gbl_clientListPanel = new GridBagLayout();
		gbl_clientListPanel.columnWidths = new int[]{73, 0};
		gbl_clientListPanel.rowHeights = new int[]{0, 92, 0};
		gbl_clientListPanel.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_clientListPanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		clientListPanel.setLayout(gbl_clientListPanel);

		clientListLabel = new JLabel("Connected (0):");
		GridBagConstraints gbc_clientListLabel = new GridBagConstraints();
		gbc_clientListLabel.insets = new Insets(0, 0, 5, 0);
		gbc_clientListLabel.gridx = 0;
		gbc_clientListLabel.gridy = 0;
		clientListPanel.add(clientListLabel, gbc_clientListLabel);

		clientListTextArea = new JTextArea();
		clientListLabel.setLabelFor(clientListTextArea);
		clientListTextArea.setEditable(false);
		clientListTextArea.setLineWrap(true);
		GridBagConstraints gbc_clientListTextArea = new GridBagConstraints();
		gbc_clientListTextArea.anchor = GridBagConstraints.NORTH;
		gbc_clientListTextArea.gridx = 0;
		gbc_clientListTextArea.gridy = 1;
		clientListPanel.add(clientListTextArea, gbc_clientListTextArea);
		clientListTextArea.setColumns(20);
		clientListTextArea.setRows(5);
		clientListTextArea.setToolTipText("who is here ?");

		JPanel buttonsPanel = new JPanel();
		GridBagConstraints gbc_buttonsPanel = new GridBagConstraints();
		gbc_buttonsPanel.insets = new Insets(0, 0, 5, 0);
		gbc_buttonsPanel.anchor = GridBagConstraints.NORTH;
		gbc_buttonsPanel.gridx = 0;
		gbc_buttonsPanel.gridy = 2;
		commandPanel.add(buttonsPanel, gbc_buttonsPanel);
		buttonsPanel.setLayout(new GridLayout(1, 2, 0, 0));

		Icon iconStart = new ImageIcon(ServerMainWindow.class.getResource("/gui/icons/start.png"));
		Icon iconStop = new ImageIcon(ServerMainWindow.class.getResource("/gui/icons/stop.png"));
		Image img = ((ImageIcon) iconStart).getImage() ;  
		Image img2 = ((ImageIcon) iconStop).getImage() ;  
		Image newimg = img.getScaledInstance(25, 25, java.awt.Image.SCALE_SMOOTH);  
		Image newimg2 = img2.getScaledInstance(25, 25, java.awt.Image.SCALE_SMOOTH);  
		iconStart = new ImageIcon(newimg);
		iconStop = new ImageIcon(newimg2);

		startServerButton = new JButton();
		startServerButton.setAction(startServerAction);
		startServerButton.setText("Start server");
		startServerButton.setIcon(iconStart);
		startServerButton.setEnabled(false);
		buttonsPanel.add(startServerButton, "cell 0 0,alignx left,aligny top");

		stopServerButton = new JButton();
		stopServerButton.setAction(stopServerAction);
		stopServerButton.setText("Stop server");
		stopServerButton.setIcon(iconStop);
		stopServerButton.setEnabled(true);
		buttonsPanel.add(stopServerButton, "cell 1 0,alignx left,aligny top");
		
		infoText = new JLabel();
		infoText.setHorizontalAlignment(SwingConstants.CENTER);
		infoText.setEnabled(false);
		GridBagConstraints gbc_infoText = new GridBagConstraints();
		gbc_infoText.fill = GridBagConstraints.BOTH;
		gbc_infoText.gridx = 0;
		gbc_infoText.gridy = 3;
		commandPanel.add(infoText, gbc_infoText);

		JSplitPane chatSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, chatTextScrollPane, chatInputScrollPane);
		chatSplitPane.setOneTouchExpandable(true);
		chatSplitPane.setResizeWeight(0.8);
		GridBagConstraints gbc_chatSplitPane = new GridBagConstraints();
		gbc_chatSplitPane.fill = GridBagConstraints.BOTH;
		gbc_chatSplitPane.insets = new Insets(0, 0, 5, 0);
		gbc_chatSplitPane.gridx = 1;
		gbc_chatSplitPane.gridy = 0;
		frame.getContentPane().add(chatSplitPane, gbc_chatSplitPane);
	}

	///////////////////////////////// ACTIONS /////////////////////////////////
	private class StartAction extends AbstractAction {
		private static final long serialVersionUID = -4916659130588867419L;
		
		public StartAction() {
			putValue(NAME, "StartAction");
			putValue(SHORT_DESCRIPTION, "Starts the server");
		}
		
		public void actionPerformed(ActionEvent e) {
//			if (server.wasStartedAndClosed()) {
//				// restart with the same port
//				int port = server.getPort();
//				server = new ServerChannel(port);
//			}
			server.startServer();
			stopServerButton.setEnabled(true);
			startServerButton.setEnabled(false);
		}
	}
	
	private class StopAction extends AbstractAction {
		private static final long serialVersionUID = 5410957576196610695L;
		
		public StopAction() {
			putValue(NAME, "StopAction");
			putValue(SHORT_DESCRIPTION, "Stops the server");
		}
		
		public void actionPerformed(ActionEvent e) {
			server.close();
			stopServerButton.setEnabled(false);
			startServerButton.setEnabled(true);
		}
	}
	
	private class ServerWindowCloser extends WindowAdapter {
		@Override
		public void windowClosing(WindowEvent e) {
			server.close();
			frame.dispose();
		}
	}

	/**
	 * Clears the client list and refills it with the given map clientNames
	 * @param clientNames : matches client SocketChannel with its name
	 * @throws IOException
	 */
	public void updateConnected(Map<SocketChannel, String> clientNames) throws IOException {
		int nb = 0;
		clientListTextArea.setText(null);
		for (SocketChannel client : clientNames.keySet()) {
			nb++;
			clientListTextArea.append(clientNames.get(client) + " (" + client.getRemoteAddress() + ")\n");
		}
		clientListLabel.setText("Connected (" + nb + "):");
	}
}

/*
Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam at tincidunt nisl. Curabitur vitae mauris laoreet, tempus eros non, sollicitudin nunc. Praesent posuere sem libero, et congue tellus elementum at. Proin quis dui neque. Suspendisse scelerisque justo eget convallis finibus. Nullam diam turpis, porta quis tortor quis, ultricies interdum turpis. Nam eget lobortis risus. Nam suscipit efficitur odio a luctus. Aliquam tristique elementum vestibulum. Nullam sodales eget libero sit amet lacinia. Nulla ultricies odio justo, sit amet rhoncus ante malesuada a. Mauris ultrices risus nec pretium tristique. In convallis, enim ut vulputate elementum, massa dui dignissim libero, vulputate dignissim libero odio vitae magna.

Ut a rutrum felis, non suscipit nunc. Praesent justo dui, fringilla at orci in, porta viverra libero. Vivamus sed lobortis eros. Phasellus pellentesque, orci elementum dictum luctus, velit est pretium odio, eu sollicitudin lorem quam ut ligula. Integer ornare interdum diam, egestas ornare leo tempor vitae. Fusce a dapibus arcu. Aliquam vel iaculis orci.

Etiam tempor eu justo eget luctus. Nullam tincidunt augue volutpat nisi rutrum, id consequat turpis venenatis. Phasellus rutrum pretium mi, vel finibus dui laoreet sit amet. Vestibulum at augue et neque eleifend accumsan blandit sit amet enim. Quisque aliquet luctus felis ut pharetra. Integer eu leo iaculis, mattis felis ac, fermentum risus. Ut dignissim, diam et tristique tristique, sem nisl sodales nisl, sit amet rhoncus ligula quam nec libero. Donec porttitor nulla nisi, id rutrum sem porttitor sed. Sed metus purus, ullamcorper sed sem vel, iaculis viverra sem. Mauris massa lorem, imperdiet sit amet maximus facilisis, vulputate non velit.

Vivamus sed nisi eu mauris rutrum elementum. Nam interdum, neque et cursus sagittis, diam odio egestas libero, sed cursus neque dolor nec nisl. Integer convallis venenatis eros, fringilla congue quam suscipit sit amet. Vestibulum at risus quis ex dignissim varius vitae non dui. Vestibulum id massa eu sapien sollicitudin ultricies. Nulla quis consequat ex. Cras ut vehicula magna, eget cursus diam. Nam elementum congue fermentum. Aenean laoreet tempor tortor, sit amet mollis augue euismod a. In tincidunt metus lectus. Duis ut hendrerit lorem, nec porta ipsum. Duis malesuada, ex et finibus consectetur, elit arcu sagittis nisl, in luctus purus mi non nunc. Nulla sodales quis odio ac finibus. Aenean eget urna dictum, imperdiet dui quis, posuere arcu. Etiam venenatis velit ut dolor euismod, ut gravida metus cursus.

Duis elementum sem fringilla neque blandit posuere. Ut et accumsan urna, in aliquam nibh. Proin neque quam, mattis sit amet turpis a, commodo lobortis purus. Maecenas pellentesque porta felis a mattis. Etiam volutpat risus ac vestibulum dignissim. Ut scelerisque nisl sit amet erat volutpat, eu vehicula lorem fermentum. Nulla dapibus turpis sit amet est porttitor luctus. 

 */