package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;

public class MenuLoseSinglePlayer extends Menu {
	private static MenuLoseSinglePlayer instance;
	private static MenuWidget exitButton, score, playAgain;
	private static ArrayList<MenuWidget> widgets;

	private MenuLoseSinglePlayer() {
		super();
	}

	public static void init(Panel root) {
		if (instance == null) {
			instance = new MenuLoseSinglePlayer();
			widgets = new ArrayList<>();

			score = new MenuString(root, "Your Score", new Font("Courier New", Font.BOLD, 75), MenuString.Side.CENTER);
			playAgain = new MenuString(root, "Play Again", new Font("Courier New", Font.BOLD, 30), MenuString.Side.CENTER,
					450);
			exitButton = new MenuString(root, "Exit", new Font("Courier New", Font.BOLD, 30), MenuString.Side.CENTER, 500);

			exitButton.setColor(Color.WHITE, Color.GREEN, Color.CYAN);
			score.setColor(Color.WHITE, Color.GREEN, Color.CYAN);
			playAgain.setColor(Color.WHITE, Color.GREEN, Color.CYAN);

			widgets.add(exitButton);
			widgets.add(score);
			widgets.add(playAgain);

		}
	}

	public static MenuLoseSinglePlayer getInstance() {
		if (instance == null) {
			throw new AssertionError("You have to call init first");
		}

		return instance;
	}

	@Override
	public void update() {
		score.setText("Your Score: " + (Board.getPlayerSnake().getSizeBody() - 1));
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

	@Override
	public void mouseAction(int button) {
		

		if (exitButton.isClick(button)) {
			Board.reset();
			Panel.setMenu(MenuStart.getInstance());
			clear();
		}

		if (playAgain.isClick(button)) {
			Board.reset();
			Board.setSizeBoard(MenuSetting.getWidgets().get(4).getEnteredNumber(), MenuSetting.getWidgets().get(5).getEnteredNumber());
			Panel.setMenu(MenuSinglePlayer.getInstance());
			clear();
		}

	}

	@Override
	public void keyAction(int keycode) {
		// TODO Auto-generated method stub

	}

}
