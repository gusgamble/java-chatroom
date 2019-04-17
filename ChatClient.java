import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Gus Gamble
 */
public class ChatClient extends ChatWindow {

	// Inner class used for networking
	private Communicator comm;

	// this is a pointer to the current chat client so communicator can change the default button once the connection has started
	private ChatClient me;

	// GUI Objects
	private JTextField serverTxt;
	private JTextField nameTxt;
	private JButton connectB;
	private JTextField messageTxt;
	private JButton sendB;

	public ChatClient() {
		super();
		this.setTitle("Chat Client");
		printMsg("Chat Client Started.");

		// GUI elements at top of window
		// Need a Panel to store several buttons/text fields
		serverTxt = new JTextField("localhost");
		serverTxt.setColumns(15);
		nameTxt = new JTextField("Name");
		nameTxt.setColumns(10);
		connectB = new JButton("Connect");
		JPanel topPanel = new JPanel();
		topPanel.add(serverTxt);
		topPanel.add(nameTxt);
		topPanel.add(connectB);
		contentPane.add(topPanel, BorderLayout.NORTH);

		// GUI elements and panel at bottom of window
		messageTxt = new JTextField("");
		messageTxt.setColumns(40);
		sendB = new JButton("Send");
		JPanel botPanel = new JPanel();
		botPanel.add(messageTxt);
		botPanel.add(sendB);
		contentPane.add(botPanel, BorderLayout.SOUTH);

		// Resize window to fit all GUI components
		this.pack();

		// with light research, found that there is a "default" button that responds to the enter key
		this.getRootPane().setDefaultButton(connectB);

		// Setup the communicator so it will handle the connect button
		Communicator comm = new Communicator();
		connectB.addActionListener(comm);
		sendB.addActionListener(comm);

		// make sure we point to the right place
		me = this;


	}

	/** This inner class handles communication with the server. */
	class Communicator implements ActionListener, Runnable {
		private Socket socket;
		private PrintWriter writer;
		private BufferedReader reader;
		private int port = 2113;

		@Override

		public void actionPerformed(ActionEvent actionEvent) {

			if (actionEvent.getActionCommand().compareTo("Connect") == 0) {
				connect();
			}
			else if (actionEvent.getActionCommand().compareTo("Send") == 0) {
				sendMsg(messageTxt.getText());
				messageTxt.setText("");	// reset the messageTxt field after a send
			}

		}

		/** Connect to the remote server and setup input/output streams. */
		public void connect() {
			try {
				socket = new Socket(serverTxt.getText(), port);
				String name = nameTxt.getText();
				InetAddress serverIP = socket.getInetAddress();
				printMsg("Connection made to " + serverIP);

				writer = new PrintWriter(socket.getOutputStream(), true);
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				// first message is always telling the server who we are
				sendMsg(name);

				// once we send the name message, the default button becomes the send button for ease of use
				me.getRootPane().setDefaultButton(sendB);

				// thread always waits to read a message. the main thread waits to send a message
				Thread t = new Thread(this);
				t.start();

			}
			catch (IOException e) {
				printMsg("\nERROR:" + e.getLocalizedMessage() + "\n");
			}
		}

		/** Receive and display a message */
		public void readMsg() throws IOException {
			String s = reader.readLine();
			printMsg(s);
		}

		/** Send a string */
		public void sendMsg(String s) {
			writer.println(s);
		}

		/**
		 * our secondary thread to read a message
		 */
		@Override

		public void run() {
			while (true) {
				try {
					readMsg();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public static void main(String args[]) {
		new ChatClient();
	}

}
