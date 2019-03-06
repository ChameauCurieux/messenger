package gui;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.Component;
import javax.swing.Box;

public class ClientMainWindow {

	private JFrame frame;
	private JTextField serverHostTextField;
	private JTextField serverPortTextField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientMainWindow window = new ClientMainWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ClientMainWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setResizable(false);
		frame.setTitle("Mini-Chat Client");
		frame.setBounds(100, 100, 387, 240);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{221, 0};
		gridBagLayout.rowHeights = new int[] {50, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		frame.getContentPane().setLayout(gridBagLayout);
		
		JPanel inputPanel = new JPanel();
		GridBagConstraints gbc_inputPanel = new GridBagConstraints();
		gbc_inputPanel.anchor = GridBagConstraints.NORTH;
		gbc_inputPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_inputPanel.insets = new Insets(0, 0, 5, 0);
		gbc_inputPanel.gridx = 0;
		gbc_inputPanel.gridy = 0;
		frame.getContentPane().add(inputPanel, gbc_inputPanel);
		GridBagLayout gbl_inputPanel = new GridBagLayout();
		gbl_inputPanel.columnWidths = new int[]{0, 185, 0, 97, 0, 0};
		gbl_inputPanel.rowHeights = new int[]{70, 0, 91, 0};
		gbl_inputPanel.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_inputPanel.rowWeights = new double[]{1.0, 0.0, 1.0, Double.MIN_VALUE};
		inputPanel.setLayout(gbl_inputPanel);
		
		Component horizontalStrut = Box.createHorizontalStrut(50);
		GridBagConstraints gbc_horizontalStrut = new GridBagConstraints();
		gbc_horizontalStrut.insets = new Insets(0, 0, 5, 5);
		gbc_horizontalStrut.gridx = 0;
		gbc_horizontalStrut.gridy = 0;
		inputPanel.add(horizontalStrut, gbc_horizontalStrut);
		
		JLabel serverHostLabel = new JLabel("Server Host");
		serverHostLabel.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_serverHostLabel = new GridBagConstraints();
		gbc_serverHostLabel.anchor = GridBagConstraints.SOUTH;
		gbc_serverHostLabel.insets = new Insets(0, 0, 5, 5);
		gbc_serverHostLabel.gridx = 1;
		gbc_serverHostLabel.gridy = 0;
		inputPanel.add(serverHostLabel, gbc_serverHostLabel);
		
		JLabel serverPortLabel = new JLabel("Server Port");
		serverPortLabel.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_serverPortLabel = new GridBagConstraints();
		gbc_serverPortLabel.anchor = GridBagConstraints.SOUTH;
		gbc_serverPortLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_serverPortLabel.insets = new Insets(0, 0, 5, 5);
		gbc_serverPortLabel.gridx = 3;
		gbc_serverPortLabel.gridy = 0;
		inputPanel.add(serverPortLabel, gbc_serverPortLabel);
		
		Component horizontalStrut_1 = Box.createHorizontalStrut(50);
		GridBagConstraints gbc_horizontalStrut_1 = new GridBagConstraints();
		gbc_horizontalStrut_1.insets = new Insets(0, 0, 5, 0);
		gbc_horizontalStrut_1.gridx = 4;
		gbc_horizontalStrut_1.gridy = 0;
		inputPanel.add(horizontalStrut_1, gbc_horizontalStrut_1);
		
		Component horizontalStrut_2 = Box.createHorizontalStrut(50);
		GridBagConstraints gbc_horizontalStrut_2 = new GridBagConstraints();
		gbc_horizontalStrut_2.insets = new Insets(0, 0, 5, 5);
		gbc_horizontalStrut_2.gridx = 2;
		gbc_horizontalStrut_2.gridy = 1;
		inputPanel.add(horizontalStrut_2, gbc_horizontalStrut_2);
		
		serverHostTextField = new JTextField();
		GridBagConstraints gbc_serverHostTextField = new GridBagConstraints();
		gbc_serverHostTextField.anchor = GridBagConstraints.NORTH;
		gbc_serverHostTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_serverHostTextField.insets = new Insets(0, 0, 0, 5);
		gbc_serverHostTextField.gridx = 1;
		gbc_serverHostTextField.gridy = 2;
		inputPanel.add(serverHostTextField, gbc_serverHostTextField);
		serverHostTextField.setColumns(10);
		
		serverPortTextField = new JTextField();
		GridBagConstraints gbc_serverPortTextField = new GridBagConstraints();
		gbc_serverPortTextField.insets = new Insets(0, 0, 0, 5);
		gbc_serverPortTextField.anchor = GridBagConstraints.NORTH;
		gbc_serverPortTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_serverPortTextField.gridx = 3;
		gbc_serverPortTextField.gridy = 2;
		inputPanel.add(serverPortTextField, gbc_serverPortTextField);
		serverPortTextField.setColumns(10);
		
		JPanel connectPanel = new JPanel();
		GridBagConstraints gbc_connectPanel = new GridBagConstraints();
		gbc_connectPanel.insets = new Insets(0, 0, 5, 0);
		gbc_connectPanel.fill = GridBagConstraints.BOTH;
		gbc_connectPanel.gridx = 0;
		gbc_connectPanel.gridy = 1;
		frame.getContentPane().add(connectPanel, gbc_connectPanel);
		
		JButton connectButton = new JButton("Connect");
		connectPanel.add(connectButton);
		
		Component verticalStrut = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut = new GridBagConstraints();
		gbc_verticalStrut.gridx = 0;
		gbc_verticalStrut.gridy = 2;
		frame.getContentPane().add(verticalStrut, gbc_verticalStrut);
	}

}
