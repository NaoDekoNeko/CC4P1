package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import service.Client;

public class MenuCreateAccount extends Menu {
	private static MenuCreateAccount instance;
	private static MenuWidget username, createButton,backButton, password, notify;
	private static ArrayList<MenuWidget> widgets;
	
	private MenuCreateAccount() {
		super();
	}
	
	public static void init(Panel root) {
		if (instance == null) {
			instance = new MenuCreateAccount();
			widgets = new ArrayList<>();
			
			
			username = new MenuEntry(root, "Enter Username: ", "", new Font("Courier New", Font.BOLD, 20),
					300, 350);

			password = new MenuEntry(root, "Enter Password: ", "", new Font("Courier New", Font.BOLD, 20), 300,
					400);

			createButton = new MenuString(root, "Create", new Font("Courier New", Font.BOLD, 30), MenuString.Side.CENTER, 450);
			
			notify = new MenuString(root, "", new Font("Courier New", Font.BOLD, 30), MenuString.Side.L, MenuString.Side.L);
			
            backButton = new MenuString(root, "Back", new Font("Courier New", Font.BOLD, 30), MenuString.Side.CENTER, 625);
            backButton.setColor(Color.WHITE, Color.YELLOW, Color.CYAN);
			
			username.setColor(Color.YELLOW, Color.WHITE, Color.CYAN, Color.BLUE);
			password.setColor(Color.YELLOW, Color.WHITE, Color.CYAN, Color.BLUE);
			createButton.setColor(Color.WHITE, Color.GREEN, Color.CYAN);
			notify.setColor(Color.RED, Color.RED, Color.RED);

			widgets.add(username);
			widgets.add(password);
			widgets.add(createButton);
			widgets.add(notify);
			widgets.add(backButton);
		}
	}
	
	public static MenuCreateAccount getInstance() {
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
		if (username.isClick(button)) {
			
		}
		
		if (password.isClick(button)) {
			
		}
		
		if (createButton.isClick(button)) {
            if (username.getEnteredString() == "" || password.getEnteredString() == "" ){
                return;
            }
            byte resByte[] = Client.sendMessage("PUT CREATE "+username.getEnteredString()+ " " +password.getEnteredString());

            int res = ByteBuffer.wrap(resByte).getInt(0);
            if(res != 0) {
				notify.setText("Create account fail !");
			}
			else {
				Panel.setMenu(MenuMultiPlayerUser.getInstance());
				clear();
			}
			
		}
		
		if (backButton.isClick(button)) {
			clear();
			notify.setText("");
			Panel.setMenu(MenuMultiPlayerUser.getInstance());
		}
		
	}

	@Override
	public void keyAction(int keycode) {
		if (username.isClicked()) {
			username.getString(keycode);
		}
		
		if (password.isClicked()) {
			password.getString(keycode);
		}
		
	}

}
