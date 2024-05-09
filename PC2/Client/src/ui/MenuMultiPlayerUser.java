package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;


public class MenuMultiPlayerUser extends Menu {
	private static MenuMultiPlayerUser instance;
	private static MenuWidget loginAccount, createAccount, backButton;
	private static ArrayList<MenuWidget> widgets;
	
	private MenuMultiPlayerUser() {
		super();
	}
	
	public static void init(Panel root) {
		if (instance == null) {
			instance = new MenuMultiPlayerUser();
			widgets = new ArrayList<>();
		
			
			loginAccount =  new MenuString(root, "Login Account", new Font("Courier New", Font.BOLD, 30), MenuString.Side.CENTER, 400);
			loginAccount.setColor( Color.WHITE, Color.CYAN, Color.BLUE);
            widgets.add(loginAccount);

            createAccount =  new MenuString(root, "Create Account", new Font("Courier New", Font.BOLD, 30), MenuString.Side.CENTER, 450);
			createAccount.setColor( Color.WHITE, Color.CYAN, Color.BLUE);
            widgets.add(createAccount);


            backButton =  new MenuString(root, "Back", new Font("Courier New", Font.BOLD, 30), MenuString.Side.CENTER, 600);
			backButton.setColor( Color.WHITE, Color.CYAN, Color.BLUE);
            widgets.add(backButton);
		}
	}
	
	
	public static MenuMultiPlayerUser getInstance() {
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
		if (createAccount.isClick(button)) {
			clear();
            Panel.setMenu(MenuCreateAccount.getInstance());
		}
		
		if (loginAccount.isClick(button)) {
            clear();
            Panel.setMenu(MenuLoginAccount.getInstance());
		}
		
		
		if (backButton.isClick(button)) {
			clear();
            Panel.setMenu(MenuStart.getInstance());
		}

		
	}

	@Override
	public void keyAction(int keycode) {

	}

}


