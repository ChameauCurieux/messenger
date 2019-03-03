package miniChat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;


public class ServerChannel {
	Selector selector;
	ServerSocketChannel serverChannel;
	Thread handler;
	boolean isRunning = false;

	public ServerChannel(int port) {
		try {
			// create new selector
			selector = Selector.open();
			// create new channel
			serverChannel = ServerSocketChannel.open();
			// bind it to the port
			InetSocketAddress address = new InetSocketAddress("localhost", port);
			serverChannel.bind(address);
			// set it to non-blocking
			serverChannel.configureBlocking(false);
			// add it to the selector (interested in "accept" events)
			serverChannel.register(selector, serverChannel.validOps());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void startServer() {
		try {
			System.out.println("Starting server " + serverChannel.getLocalAddress());
			isRunning = true;
			// creating new handler
			handler = new Thread(new Runnable () {

				@Override
				public void run() {
					while(isRunning) {
						try {
							selector.select();
							// checks whether the selector is open
							if (!selector.isOpen()) {
								break;
							}
							Set<SelectionKey> selectedKeys = selector.selectedKeys();
							Iterator<SelectionKey> iterator = selectedKeys.iterator();

							while (iterator.hasNext()) {
								SelectionKey key = iterator.next();
								iterator.remove();

								if(key.isAcceptable()) {
									// creating new connection
									SocketChannel clientChannel = serverChannel.accept();
									clientChannel.configureBlocking(false);
									// adding it to the selector
									clientChannel.register(selector, clientChannel.validOps());
									System.out.println("Added client " + clientChannel.getRemoteAddress());
								}

								if(key.isConnectable()) {
									((SocketChannel)key.channel()).finishConnect();
								}

								if(key.isReadable()) {
									// read message
									SocketChannel clientChannel = (SocketChannel) key.channel();
									ByteBuffer buffer = ByteBuffer.allocate(256);
									int nbBytesRead = clientChannel.read(buffer);
									// if connection closed client side
									if (nbBytesRead == -1) {
										continue;
									}
									byte[] byteArray = buffer.array();
									String content = new String(byteArray).trim();
									System.out.println("Received message : \"" + content + "\", from : " + clientChannel.getRemoteAddress());
								}

								if(key.isWritable()) {
									// send message
								}
							}
						} catch (IOException | CancelledKeyException | ClosedSelectorException e) {
							e.printStackTrace();
						}
					}
				}

			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		// start the handler
		handler.start();
	}

	public void stopServer() {
		System.out.println("Closing the server...");
		isRunning = false;
		try {
			for (SelectionKey key : selector.keys()) {
				SelectableChannel channel = key.channel();
				if (channel.equals(serverChannel)) {
					serverChannel.socket().close();			
					serverChannel.close();
					System.out.println("	Closing server socket");
				}
				else {
					SocketChannel clientChannel = (SocketChannel) channel;
					Socket clientSocket = clientChannel.socket();
					System.out.println("	Closing connection to client " + clientSocket.getRemoteSocketAddress());
					clientSocket.close();
					clientChannel.close();
				}
				key.cancel();
			}
			selector.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}