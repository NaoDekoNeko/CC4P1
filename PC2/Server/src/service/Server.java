package service;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

import util.SnakeDirection;

public class Server {
	private static Selector selector;
	private static SelectionKey key;
	private static ByteBuffer buffer;
	private static ServerSocketChannel listenSocket;
	private static SocketChannel clientSocket;
	private static Set<SelectionKey> selectedKeys;
	private static Iterator<SelectionKey> iter;
	private static Server instance;
	private static boolean run = true;
	private static String[] splitedCommand;
	private static final int LEN_BUFFER = 4096;

	private Server(int port) throws IOException {
		Server.selector = Selector.open();
		Server.listenSocket = ServerSocketChannel.open();
		Server.listenSocket.bind(new InetSocketAddress(port));
		Server.listenSocket.configureBlocking(false);
		Server.listenSocket.register(selector, SelectionKey.OP_ACCEPT);
		Server.buffer = ByteBuffer.allocate(LEN_BUFFER);

	}

	public static void start(int port) throws IOException {
		if (instance == null)
			instance = new Server(port);

		System.out.println("server ip:" + InetAddress.getLocalHost().getHostAddress());
		System.out.println("port:" + port);
		while (run) {
			// wait for events
			Server.selector.select();

			// if an event occurs
			Server.selectedKeys = Server.selector.selectedKeys();
			Server.iter = Server.selectedKeys.iterator();
			while (Server.iter.hasNext()) {
				Server.key = Server.iter.next();
				if (Server.key.isAcceptable()) {
					Server.register(selector, Server.listenSocket);
				}

				if (Server.key.isReadable()) {
					Server.handleClient(Server.key);

				}
				Server.iter.remove();
			}
		}
	}

	public static int getLenBuffer() {
		return Server.LEN_BUFFER;
	}

	private static void register(Selector selector, ServerSocketChannel serverSocket) throws IOException {
		Server.clientSocket = serverSocket.accept();
		System.out.println("client connected " + clientSocket.getRemoteAddress());
		Server.clientSocket.configureBlocking(false);
		Server.clientSocket.register(selector, SelectionKey.OP_READ);
	}

	private static void handleClient(SelectionKey key) throws IOException {
		Server.clientSocket = (SocketChannel) key.channel();
		Server.clearBuffer();
		try {
			int len = Server.clientSocket.read(Server.buffer);
			// Server.clientSocket.read(null, LEN_BUFFER, LEN_BUFFER)
		} catch (Exception e) {
			key.cancel();
			System.out.println(Server.clientSocket.getRemoteAddress() + " disconnected !");
			try {
				Users.erase(clientSocket);
			} catch (Exception e1) {

			}

			return;
		}

		Server.splitedCommand = new String(Server.buffer.array()).trim().split("\\s+");
		Server.clearBuffer();

		switch (splitedCommand[0].trim()) {
			case "GET":
				Server.getHandle();
				break;

			case "PUT":
				Server.putHandle();
				break;

			default:
				break;
		}

		Server.clientSocket.write(Server.buffer);
		Server.clearBuffer();
	}

	private static void getHandle() {

		switch (splitedCommand[1].trim()) {
			case "SIZE_BOARD":
				Server.buffer.putInt(0, Users.getServerBoard(clientSocket).getSizeBoard()[0]);
				Server.buffer.putInt(4, Users.getServerBoard(clientSocket).getSizeBoard()[1]);
				break;

			case "UPDATE_BOARD":
				Server.buffer.put(0, Users.getUpdateGrid(Server.clientSocket));
				break;

			case "SCORE":
				Server.buffer.putInt(0, Users.getSnake(Server.clientSocket).getBodySize());
				break;

			default:
				break;
		}
	}

	private static void putHandle() {

		switch (splitedCommand[1].trim()) {
			case "CREATE_BOARD":
				String roomID = Users.createRoom(Server.clientSocket, Integer.parseInt(splitedCommand[2]),
						Integer.parseInt(splitedCommand[3]), Integer.parseInt(splitedCommand[4]),
						Integer.parseInt(splitedCommand[5]));
				Server.buffer.put(0, roomID.getBytes());
				break;

			case "JOIN_ROOM":
				Server.buffer.put(0, Users.joinRoom(clientSocket, splitedCommand[2].trim()));
				break;

			case "LOGIN":
				Server.buffer.putInt(0, Users.loginAccount(splitedCommand[2].trim(), splitedCommand[3].trim()));
				break;

			case "CREATE":
				Server.buffer.putInt(0, Users.createAccount(splitedCommand[2].trim(), splitedCommand[3].trim()));
				break;

			case "UP":
				Users.getSnake(Server.clientSocket).changeDirection(SnakeDirection.UP);
				break;
			case "DOWN":
				Users.getSnake(Server.clientSocket).changeDirection(SnakeDirection.DOWN);
				break;

			case "LEFT":
				Users.getSnake(Server.clientSocket).changeDirection(SnakeDirection.LEFT);
				break;

			case "RIGHT":
				Users.getSnake(Server.clientSocket).changeDirection(SnakeDirection.RIGHT);
				break;

			default:
				break;
		}
	}

	private static void clearBuffer() {
		Server.buffer.put(0, new byte[Server.LEN_BUFFER]);
		Server.buffer.clear();
	}
}
