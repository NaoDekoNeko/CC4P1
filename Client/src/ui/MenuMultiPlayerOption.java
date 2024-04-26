package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;

public class MenuMultiPlayerOption extends Menu {
	private static MenuMultiPlayerOption instance;
	private static MenuWidget serverAddress, connectButton,backButton, serverPort, notify;
	private static ArrayList<MenuWidget> widgets;
	
	private MenuMultiPlayerOption() {
		super();
	}
	
	public static void init(Panel root) {
		if (instance == null) {
			instance = new MenuMultiPlayerOption();
			widgets = new ArrayList<>();
			
			
			serverAddress = new MenuEntry(root, "Enter Server IP: ", "127.0.0.1", new Font("Courier New", Font.BOLD, 20),
					300, 350);

			serverPort = new MenuEntry(root, "Enter Server Port: ", "3333", new Font("Courier New", Font.BOLD, 20), 300,
					400);

			connectButton = new MenuString(root, "Connect", new Font("Courier New", Font.BOLD, 30), MenuString.Side.CENTER, 450);
			
			notify = new MenuString(root, "", new Font("Courier New", Font.BOLD, 30), MenuString.Side.L, MenuString.Side.L);
			
            backButton = new MenuString(root, "Back", new Font("Courier New", Font.BOLD, 30), MenuString.Side.CENTER, 625);
            backButton.setColor(Color.WHITE, Color.YELLOW, Color.CYAN);
			
			serverAddress.setColor(Color.YELLOW, Color.WHITE, Color.CYAN, Color.BLUE);
			serverPort.setColor(Color.YELLOW, Color.WHITE, Color.CYAN, Color.BLUE);
			connectButton.setColor(Color.WHITE, Color.GREEN, Color.CYAN);
			notify.setColor(Color.RED, Color.RED, Color.RED);

			widgets.add(serverAddress);
			widgets.add(serverPort);
			widgets.add(connectButton);
			widgets.add(notify);
			widgets.add(backButton);
		}
	}
	
	public static MenuMultiPlayerOption getInstance() {
		if (instance == null) {
			throw new AssertionError("You have to call init first");
		}

		return instance;
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

	@Override
	public void mouseAction(int button) {
		if (serverAddress.isClick(button)) {
			
		}
		
		if (serverPort.isClick(button)) {
			
		}
		
		if (connectButton.isClick(button)) {
			if(Board.connectServer(serverAddress.getEnteredString(), serverPort.getEnteredNumber()) != 0) {
				notify.setText("Can't connect to Server !");
			}
			else {
				Panel.setMenu(MenuMultiPlayerUser.getInstance());
				clear();
			}
			
		}
		
		if (backButton.isClick(button)) {
			clear();
			notify.setText("");
			Panel.setMenu(MenuStart.getInstance());
		}
		
	}

	@Override
	public void keyAction(int keycode) {
		if (serverAddress.isClicked()) {
			serverAddress.getString(keycode);
		}
		
		if (serverPort.isClicked()) {
			serverPort.getNumber(keycode);
		}
		
	}

}
