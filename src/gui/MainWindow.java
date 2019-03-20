package gui;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public abstract class MainWindow {
	protected JFrame frame;
	protected JTextArea chatTextArea;
	protected JLabel infoText;
	protected JTextField addressTextField;

	public void addMessage(String string) {
		chatTextArea.append(string + "\n");
		chatTextArea.setCaretPosition(chatTextArea.getDocument().getLength());
	}
	public void setInfo(String string) {
		infoText.setText(string);
	}
	public void setAddress(String string) {
		addressTextField.setText(string);
	}
	public void setVisible(boolean b) {
		frame.setVisible(b);
	}

}
