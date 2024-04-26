package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import util.SnakeDirection;

public class MenuSinglePlayer extends Menu {
	private static MenuSinglePlayer instance;
	private static MenuWidget exitButton, score, level;
	private static ArrayList<MenuWidget> widgets;

	private MenuSinglePlayer() {

		super();
	}

	public static void init(Panel root) {
		if (instance == null) {
			instance = new MenuSinglePlayer();
			widgets = new ArrayList<>();

			exitButton = new MenuString(root, "Exit", new Font("Courier New", Font.BOLD, 30), 900, 50);
			score = new MenuString(root, "Score: 0", new Font("Courier New", Font.BOLD, 30), MenuString.Side.CENTER,
					50);
			level = new MenuString(root, "Level: " + Board.level, new Font("Courier New", Font.BOLD, 30),
					MenuString.Side.CENTER, 100);

			exitButton.setColor(Color.WHITE, Color.GREEN, Color.CYAN);
			score.setColor(Color.WHITE, Color.GREEN, Color.CYAN);

			widgets.add(Board.getInstance());
			widgets.add(score);
			widgets.add(exitButton);
			widgets.add(level);

		}
	}

	public static MenuSinglePlayer getInstance() {
		if (instance == null) {
			throw new AssertionError("You have to call init first");
		}

		return instance;
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
				Board.getPlayerSnake().changeDirection(SnakeDirection.LEFT);
				break;
			case KeyEvent.VK_RIGHT, KeyEvent.VK_D:
				Board.getPlayerSnake().changeDirection(SnakeDirection.RIGHT);
				break;
			case KeyEvent.VK_UP, KeyEvent.VK_W:
				Board.getPlayerSnake().changeDirection(SnakeDirection.UP);
				break;
			case KeyEvent.VK_DOWN, KeyEvent.VK_S:
				Board.getPlayerSnake().changeDirection(SnakeDirection.DOWN);
				break;
		}

	}

	@Override
	public void update() {
		score.setText("Score: " + Board.getPlayerSnake().getSizeBody());
		level.setText("Level: " + Board.level);

		if (Board.getPlayerSnake().isDead()) {
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
