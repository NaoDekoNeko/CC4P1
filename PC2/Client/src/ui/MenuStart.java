package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;

public class MenuStart extends Menu {
	private static MenuStart instance;
	private static MenuWidget titleGame, titleSinglePlayer, titleMultiPLayer, mainSetting, exitButton;
	private static ArrayList<MenuWidget> widgets;

	private MenuStart() {
		super();
		widgets = new ArrayList<>();

	}

	public static void init(Panel root) {
		if (instance == null) {
			instance = new MenuStart();
			MenuStart.titleGame = new MenuString(root, "Snake", new Font("Courier New", Font.BOLD, Config.TITLE_SIZE),
					MenuString.Side.CENTER);
			MenuStart.titleSinglePlayer = new MenuString(root, "Single Player", new Font("Courier New", Font.BOLD, 30),
					MenuString.Side.CENTER, 485);
			MenuStart.titleMultiPLayer = new MenuString(root, "Multiplayer", new Font("Courier New", Font.BOLD, 30),
					MenuString.Side.CENTER, 525);
			MenuStart.mainSetting = new MenuString(root, "Setting", new Font("Courier New", Font.BOLD, 30),
					MenuString.Side.CENTER, 575);
			MenuStart.exitButton = new MenuString(root, "Exit", new Font("Courier New", Font.BOLD, 30),
					MenuString.Side.CENTER, 625);

			MenuStart.titleGame.setColor(Color.WHITE, Color.RED, Color.CYAN);
			MenuStart.titleSinglePlayer.setColor(Color.WHITE, Color.GREEN, Color.CYAN);
			MenuStart.titleMultiPLayer.setColor(Color.WHITE, Color.GREEN, Color.CYAN);
			MenuStart.mainSetting.setColor(Color.WHITE, Color.GREEN, Color.CYAN);
			MenuStart.exitButton.setColor(Color.WHITE, Color.GREEN, Color.CYAN);

			MenuStart.widgets.add(titleGame);
			MenuStart.widgets.add(titleSinglePlayer);
			MenuStart.widgets.add(titleMultiPLayer);
			MenuStart.widgets.add(mainSetting);
			MenuStart.widgets.add(exitButton);
		}

	}

	public static MenuStart getInstance() {
		if (instance == null) {
			throw new AssertionError("You have to call init first");
		}

		return instance;
	}

	@Override
	public void mouseAction(int button) {

		if (titleGame.isClick(button)) {
		}

		if (mainSetting.isClick(button)) {
			Panel.setMenu(MenuSetting.getInstance());
			clear();
		}

		if (exitButton.isClick(button)) {
			Frame.close();
		}

		if (titleSinglePlayer.isClick(button)) {
			Board.setSizeBoard(MenuSetting.getWidgets().get(4).getEnteredNumber(),
					MenuSetting.getWidgets().get(5).getEnteredNumber());
			Panel.setMenu(MenuSinglePlayer.getInstance());
			clear();
		}

		if (titleMultiPLayer.isClick(button)) {
			Panel.setMenu(MenuMultiPlayerOption.getInstance());
			clear();
		}

	}

	@Override
	public void keyAction(int keycode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void update() {
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
