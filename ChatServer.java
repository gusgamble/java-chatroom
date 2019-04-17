
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Gus Gamble
 */
public class ChatServer extends ChatWindow {

	ArrayList<ClientHandler> clientHandlerList = new ArrayList<>();

	public ChatServer() {
		super();
		this.setTitle("Chat Server");
		this.setLocation(80, 80);


		try {
			// Create a listening service for connections
			// at the designated port number.
			ServerSocket srv = new ServerSocket(2113);

			printMsg("Waiting for a connection");
			while (true) {
				// The method accept() blocks until a client connects.
				// printMsg("Waiting for a connection");
				Socket socket = srv.accept();

				// instead of creating 1 new clientHandler, all requests are accepted and get their own thread.
				// note that the main thread still always waits for new connections
				Thread t = new Thread(new ClientHandler(socket));
				t.start();
			}

		} catch (IOException e) {
			System.out.println(e);
		}
	}

	/**
	 * This inner class handles communication to/from one client.
	 */
	class ClientHandler implements Runnable {
		private PrintWriter writer;
		private BufferedReader reader;
		String name;

		public ClientHandler(Socket socket) {
			try {
				InetAddress serverIP = socket.getInetAddress();
				printMsg("Connection made to " + serverIP);
				writer = new PrintWriter(socket.getOutputStream(), true);
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				// first message is always the user's name
				this.name = reader.readLine();

				// now we have a name and all variables assigned, add to the list of users
				clientHandlerList.add(this);

				// add to history and send to all the name of the new client
				printMsg(name + " has joined");
				sendMsg(name + " has joined");

			} catch (IOException e) {
				printMsg("\nERROR:" + e.getLocalizedMessage() + "\n");
			}
		}

		public void handleConnection() {
			try {
				while (true) {
					// read a message from the client and send it to all
					readMsg();
				}
			} catch (Exception e) {
				// remove the user from the list of people to send to
				clientHandlerList.remove(this);
				// let everyone know that the user has left
				sendMsg(this.name + " has left the chatroom");
				printMsg(this.name + " has left the chatroom");

			} finally {
				// join the thread if there is no more user for it
				try {
					Thread.currentThread().join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		/** Receive and display a message */
		public void readMsg() throws IOException {

			String s = reader.readLine();
			// if the /name command is detected at the beginning
			if (s.startsWith("/name")) {
				// save the old name so we can print it back
				String oldName = this.name;
				// reassign the name
				this.name = s.replaceFirst("/name ", "");
				// send the message and save the change to the server history
				printMsg(oldName + " changed their name to " + this.name);
				sendMsg(oldName + " changed their name to " + this.name);
				return;
			}
			// otherwise save the history and send to the users
			printMsg(this.name + ": " + s);
			sendMsg(this.name + ": " + s);

		}

		/** Send a string */
		public void sendMsg(String s) {
			// send to all users
			for (ClientHandler c : clientHandlerList) {
				c.writer.println(s);
			}

		}

		/**
		 * our secondary thread to handle each individual connection
		 */
		@Override

		public void run() {
			while (true) {
				handleConnection();
			}
		}

	}

	public static void main(String args[]) {
		new ChatServer();
	}

}
