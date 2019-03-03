package miniChat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.Channel;
import java.nio.channels.ClosedSelectorException;
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
									clientChannel.read(buffer);
									byte[] byteArray = buffer.array();
									String content = new String(byteArray).trim();
									System.out.println("Received message : \"" + content + "\", from : " + clientChannel.getRemoteAddress());
								}

								if(key.isWritable()) {
									// send message
								}

								// once the channel key has been handled, we remove it from the set
								iterator.remove();
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
			/*serverChannel.socket().close();
			serverChannel.close();*/
			for (SelectionKey key : selector.keys()) {
				// TODO java.lang.ClassCastException: class sun.nio.ch.ServerSocketChannelImpl cannot be cast to class java.nio.channels.SocketChannel
				SocketChannel channel = (SocketChannel) key.channel();
				channel.socket().close();
				channel.close();
			}
			selector.close();
			// TODO loop doesn't stop immediately
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}