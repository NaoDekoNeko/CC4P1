package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import service.Client;

public class MenuMultiPlayer extends Menu{
	private static MenuMultiPlayer instance;
	private static MenuWidget exitButton, scoreTitle, infoRoom;
	private static ArrayList<MenuWidget> widgets;

	private MenuMultiPlayer() {

		super();
	}

	public static void init(Panel root) {
		if (instance == null) {
			instance = new MenuMultiPlayer();
			widgets = new ArrayList<>();
			

			exitButton = new MenuString(root, "Exit", new Font("Courier New", Font.BOLD, 30), 900, 50);
			scoreTitle = new MenuString(root, "Score: 0", new Font("Courier New", Font.BOLD, 30), MenuString.Side.CENTER, 100);
			infoRoom = new MenuString(root, "ID: ", new Font("Courier New", Font.BOLD, 30), MenuString.Side.CENTER, 50);

			exitButton.setColor(Color.WHITE, Color.GREEN, Color.CYAN);
			scoreTitle.setColor(Color.WHITE, Color.GREEN, Color.CYAN);
			infoRoom.setColor(Color.WHITE, Color.GREEN, Color.CYAN);

			widgets.add(Board.getInstance());
			widgets.add(scoreTitle);
			widgets.add(exitButton);
			widgets.add(infoRoom);

		}
	}
	
	public static void setStringInfo(String roomid) {
		infoRoom.setText(roomid);
	}

	public static MenuMultiPlayer getInstance() {
		if (instance == null) {
			throw new AssertionError("You have to call init first");
		}

		return instance;
	}
	
	private int fromByteArray(byte[] bytes) {
	     return ((bytes[0] & 0xFF) << 24) | 
	            ((bytes[1] & 0xFF) << 16) | 
	            ((bytes[2] & 0xFF) << 8 ) | 
	            ((bytes[3] & 0xFF) << 0 );
	}

	@Override
	public void mouseAction(int button) {
		if (exitButton.isClick(button)) {
			Board.reset();
			Panel.setMenu(MenuStart.getInstance());
			clear();
		}

	}

	@Override
	public void keyAction(int keycode) {
		switch (keycode) {
			case KeyEvent.VK_LEFT, KeyEvent.VK_A:
				Client.sendMessage("PUT LEFT");
				break;
			case KeyEvent.VK_RIGHT, KeyEvent.VK_D:
				Client.sendMessage("PUT RIGHT");
				break;
			case KeyEvent.VK_UP, KeyEvent.VK_W:
				Client.sendMessage("PUT UP");
				break;
			case KeyEvent.VK_DOWN, KeyEvent.VK_S:
				Client.sendMessage("PUT DOWN");
				break;
			default:
				break;
		}

	}

	@Override
	public void update() {
		int score = fromByteArray(Client.sendMessage("GET SCORE"));
		scoreTitle.setText("Score: " + score);

		if (!Board.isConnected() && Board.getPlayerSnake().isDead() ) {
			clear();
			Panel.setMenu(MenuLoseSinglePlayer.getInstance());
		}
		for (MenuWidget widget : widgets) {
			widget.update();
		}

	}

	@Override
	public void draw(Graphics g) {
		for (MenuWidget widget : widgets) {
			widget.active();
			widget.draw(g);
		}

	}

	@Override
	public void clear() {
		for (MenuWidget widget : widgets) {
			widget.disable();
		}

	}

}
