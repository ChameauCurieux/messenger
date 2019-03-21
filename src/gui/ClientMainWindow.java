package gui;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.SocketAddress;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import miniChat.Client;

/**
 * Displays informations and controls for a client.
 * Its content is updated by the associated client.
 * @author juju
 *
 */
public class ClientMainWindow extends MainWindow{

	private JTextArea chatInputTextArea;
	private JTextArea clientListTextArea;
	private JLabel clientListLabel;
	private JTextField nameTextField;
	private JButton sendButton;
	private Client client;
	private final Action sendAction = new SendAction();


	/**
	 * Launch the application.
	 */
	//	public static void main(String[] args) {
	//		EventQueue.invokeLater(new Runnable() {
	//			public void run() {
	//				try {					
	//					clientMainWindow.instance = new clientMainWindow();
	//					clientMainWindow.frmMinichatclient.setVisible(true);
	//				} catch (Exception e) {
	//					e.printStackTrace();
	//				}
	//			}
	//		});
	//	}

	/**
	 * Create the application with a new client
	 * @throws IOException 
	 * @wbp.parser.constructor 
	 */
	public ClientMainWindow(SocketAddress ad, String name) throws IOException {
		this(new Client(ad, name));
	}
	public ClientMainWindow(SocketAddress address) throws IOException {
		this(new Client(address));
	}

	/**
	 * Create the application, linked to an existing client
	 */
	public ClientMainWindow(Client c) {
		client = c;
		initialize();
		client.setWindow(this);
		client.startClient();
	}
	
	public Client getClient() {
		return client;
	}


	/**
	 * Initialize the contents of the frame.
	 * @throws IOException 
	 */
	private void initialize() {

		frame = new JFrame();
		frame.setResizable(false);
		frame.setTitle("Mini-Chat : client");
		frame.setBounds(100, 100, 608, 302);
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new clientWindowCloser());
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowWeights = new double[]{1.0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0};
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


		JPanel commandPanel = new JPanel();
		GridBagConstraints gbc_commandPanel = new GridBagConstraints();
		gbc_commandPanel.insets = new Insets(0, 0, 0, 5);
		gbc_commandPanel.gridx = 0;
		gbc_commandPanel.gridy = 0;
		frame.getContentPane().add(commandPanel, gbc_commandPanel);
		GridBagLayout gbl_commandPanel = new GridBagLayout();
		gbl_commandPanel.columnWidths = new int[]{86, 0};
		gbl_commandPanel.rowHeights = new int[]{0, 38, 0, 0, 0, 0};
		gbl_commandPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_commandPanel.rowWeights = new double[]{0.0, 0.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
		commandPanel.setLayout(gbl_commandPanel);
		
				JPanel addressPanel = new JPanel();
				addressPanel.setToolTipText("where the clients need to connect");
				GridBagConstraints gbc_addressPanel = new GridBagConstraints();
				gbc_addressPanel.fill = GridBagConstraints.HORIZONTAL;
				gbc_addressPanel.insets = new Insets(0, 0, 5, 0);
				gbc_addressPanel.gridx = 0;
				gbc_addressPanel.gridy = 0;
				commandPanel.add(addressPanel, gbc_addressPanel);
				addressPanel.setLayout(new GridLayout(0, 1, 0, 0));
				
						JLabel addressTipLabel = new JLabel("client address :");
						addressTipLabel.setVerticalAlignment(SwingConstants.BOTTOM);
						addressTipLabel.setHorizontalAlignment(SwingConstants.CENTER);
						addressPanel.add(addressTipLabel);
						
								addressTextField = new JTextField();
								addressTipLabel.setLabelFor(addressTextField);
								addressTextField.setEditable(false);
								addressPanel.add(addressTextField);
								addressTextField.setHorizontalAlignment(SwingConstants.CENTER);
		
				JPanel namePanel = new JPanel();
				GridBagConstraints gbc_namePanel = new GridBagConstraints();
				gbc_namePanel.insets = new Insets(0, 0, 5, 0);
				gbc_namePanel.gridx = 0;
				gbc_namePanel.gridy = 1;
				commandPanel.add(namePanel, gbc_namePanel);
				namePanel.setToolTipText("where the clients need to connect");
				namePanel.setLayout(new GridLayout(0, 1, 0, 0));
				
						JLabel nameLabel = new JLabel("client name :");
						nameLabel.setVerticalAlignment(SwingConstants.BOTTOM);
						nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
						namePanel.add(nameLabel);
						
								nameTextField = new JTextField();
								nameTextField.setEditable(false);
								nameTextField.setHorizontalAlignment(SwingConstants.CENTER);
								namePanel.add(nameTextField);

		JPanel clientListPanel = new JPanel();
		clientListPanel.setToolTipText("");
		GridBagConstraints gbc_clientListPanel = new GridBagConstraints();
		gbc_clientListPanel.insets = new Insets(0, 0, 5, 0);
		gbc_clientListPanel.gridx = 0;
		gbc_clientListPanel.gridy = 2;
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

		infoText = new JLabel();
		infoText.setHorizontalAlignment(SwingConstants.CENTER);
		infoText.setEnabled(false);
		GridBagConstraints gbc_infoText = new GridBagConstraints();
		gbc_infoText.fill = GridBagConstraints.VERTICAL;
		gbc_infoText.gridx = 0;
		gbc_infoText.gridy = 4;
		commandPanel.add(infoText, gbc_infoText);

		JPanel inputPanel = new JPanel();
		GridBagLayout gbl_inputPanel = new GridBagLayout();
		gbl_inputPanel.columnWidths = new int[]{388, 0, 0};
		gbl_inputPanel.rowHeights = new int[]{77, 0};
		gbl_inputPanel.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_inputPanel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		inputPanel.setLayout(gbl_inputPanel);

		JSplitPane chatSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, chatTextScrollPane, inputPanel);
		chatSplitPane.setOneTouchExpandable(true);
		chatSplitPane.setResizeWeight(0.8);
		GridBagConstraints gbc_chatSplitPane = new GridBagConstraints();
		gbc_chatSplitPane.fill = GridBagConstraints.BOTH;
		gbc_chatSplitPane.gridx = 1;
		gbc_chatSplitPane.gridy = 0;
		frame.getContentPane().add(chatSplitPane, gbc_chatSplitPane);

		chatInputTextArea = new JTextArea();
		chatInputTextArea.setWrapStyleWord(true);
		chatInputTextArea.setLineWrap(true);
		chatInputTextArea.setTabSize(4);
		chatInputTextArea.requestFocusInWindow();

		chatInputTextArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
        		// TAB changes the focus instead of adding in the text
                if (e.getKeyCode() == KeyEvent.VK_TAB) {
                	// SHIFT+TAB changes the focus backward
                    if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == 1) {
                    	chatInputTextArea.transferFocusBackward();
                    } else {
                    	chatInputTextArea.transferFocus();
                    }
                    e.consume();
                }
                else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    // line is added with CTRL+ENTER
                	if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == 1){
                		chatInputTextArea.append("\n");
                	}
            		// ENTER without modifier sends the messages instead of adding a line
                	else if ((e.getModifiersEx() & (InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK))== 0) {
                		sendAction.actionPerformed(null);
                	}
                    e.consume();
                }
            }
        });
		
		JScrollPane chatInputScrollPane = new JScrollPane(chatInputTextArea);
		chatInputScrollPane.setToolTipText("type your message here (CTRL+ENTER for line return)");
		GridBagConstraints gbc_chatInputScrollPane = new GridBagConstraints();
		gbc_chatInputScrollPane.weighty = 1.0;
		gbc_chatInputScrollPane.weightx = 1.0;
		gbc_chatInputScrollPane.insets = new Insets(0, 0, 0, 5);
		gbc_chatInputScrollPane.fill = GridBagConstraints.BOTH;
		gbc_chatInputScrollPane.gridx = 0;
		gbc_chatInputScrollPane.gridy = 0;
		inputPanel.add(chatInputScrollPane, gbc_chatInputScrollPane);

		sendButton = new JButton();
		sendButton.setToolTipText("send message");
		sendButton.setAction(sendAction);
		sendButton.setIcon(new ImageIcon(ClientMainWindow.class.getResource("/gui/icons/send_arrow.png")));
		sendButton.setText("Send");
		GridBagConstraints gbc_sendButton = new GridBagConstraints();
		gbc_sendButton.fill = GridBagConstraints.BOTH;
		gbc_sendButton.gridx = 1;
		gbc_sendButton.gridy = 0;
		inputPanel.add(sendButton, gbc_sendButton);
		// add keyboard shortcut
		KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		sendButton.getInputMap().put(enter, "sendMessage");
		sendButton.getActionMap().put("sendMessage", sendAction);
		
	}

	private class clientWindowCloser extends WindowAdapter {
		@Override
		public void windowClosing(WindowEvent e) {
			client.close();
			frame.dispose();
		}
	}
	
	private class SendAction extends AbstractAction {
		private static final long serialVersionUID = -7786195937926838770L;
		public SendAction() {
			putValue(NAME, "SendAction");
			putValue(SHORT_DESCRIPTION, "Sends the message in the input area to the server");
		}
		public void actionPerformed(ActionEvent e) {
			String message = chatInputTextArea.getText().toString();
			chatInputTextArea.setText(null);
			client.sendMessage(message);
		}
	}

	public void setConnected(boolean b) {
		chatInputTextArea.setEnabled(false);
		sendButton.setEnabled(false);
	}
	public void setName(String name) {
		nameTextField.setText(name);
	}
}