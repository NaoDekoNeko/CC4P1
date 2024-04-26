package service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import model.Board;

public class BoardManager implements Runnable {
	private static HashMap<String, Board> boardMap = new HashMap<>();
	private static BoardManager instance;
	private static final String AlphaNumericString = "0123456789" + "ABCDEFGHIJKLMNOPQRSTUVXYZ";
	private static final int lengthID = 6;
	private static Thread thread;
	private boolean running = false;

	private BoardManager() {
		running = true;
	}

	public static synchronized String addBoard(Board board) {
		StringBuilder sb = new StringBuilder(lengthID);
		do {
			for (int i = 0; i < lengthID; i++) {
				int index = (int) (AlphaNumericString.length() * Math.random());
				sb.append(AlphaNumericString.charAt(index));
			}
		} while (boardMap.containsKey(sb.toString()));
		boardMap.put(sb.toString(), board);
		return sb.toString();
	}

	public static synchronized Board getBoard(String roomID) {
		return boardMap.get(roomID);
	}

	public static BoardManager getInstace() {
		if (instance == null) {
			instance = new BoardManager();
		}
		return BoardManager.instance;
	}

	public void start() {
		thread = new Thread(this);
		thread.start();
	}

	private static synchronized void update() {
		for (Iterator<Entry<String, Board>> i = boardMap.entrySet().iterator(); i.hasNext();) {
			Map.Entry<String, Board> element = i.next();
			if (element.getValue().countSnake() == 0) {
				i.remove();
			} else {
				element.getValue().update();
			}
		}
	}

	@Override
	public void run() {
		while (running) {
			BoardManager.update();
		}

	}

}
