package miniChat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Server {
	int port;
	List<Socket> listClients;
	boolean isRunning = false;
	Thread handler;
	ServerSocket servSock;

	public Server(int p) {
		try {
			port = p;
			servSock = new ServerSocket(port);			
		} catch (IOException e) {
			e.printStackTrace();
		}
		listClients = Collections.synchronizedList(new ArrayList<Socket>());		
	}

	public void startServer() {
		isRunning = true;
		handler = new Thread(new Runnable() {

			@Override
			public void run() {
				Socket clientSock;
				try {
					while (isRunning) {
						clientSock = servSock.accept();
						listClients.add(clientSock);
						System.out.println("Added client " + clientSock.getRemoteSocketAddress());
					}
				} catch (IOException e) {
					e.printStackTrace();
					isRunning = false;
				}
			}

		});
		handler.start();
		
		// close up
		for (Socket sock : listClients) {
			try {
				sock.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void stopServer() {
		isRunning = false;
	}

	public int getPort() {
		return port;
	}
}
