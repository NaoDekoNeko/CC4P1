package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;


public class MenuMultiPlayerRoomOption extends Menu{
	private static MenuMultiPlayerRoomOption instance;
	private static MenuWidget joinRoom, backButton;
	private static MenuWidget createRoom;
	private static ArrayList<MenuWidget> widgets;
	
	private MenuMultiPlayerRoomOption() {
		super();
	}
	
	
	public static void init(Panel root) {
		if (instance == null) {
			instance = new MenuMultiPlayerRoomOption();
			widgets = new ArrayList<>();
			
			
			joinRoom = new MenuString(root, "Join Room", new Font("Courier New", Font.BOLD, 30),
					MenuString.Side.CENTER, 425);

			createRoom = new MenuString(root, "Create Room", new Font("Courier New", Font.BOLD, 30),
					MenuString.Side.CENTER, 475);
			
			backButton = new MenuString(root, "Back", new Font("Courier New", Font.BOLD, 30),
					MenuString.Side.CENTER, 600);
			
			joinRoom.setColor(Color.WHITE, Color.GREEN, Color.CYAN);
			backButton.setColor(Color.WHITE, Color.GREEN, Color.CYAN);
			createRoom.setColor(Color.WHITE, Color.GREEN, Color.CYAN);

			widgets.add(joinRoom);
			widgets.add(createRoom);
			widgets.add(backButton);
		}
	}
	
	
	public static MenuMultiPlayerRoomOption getInstance() {
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
		if (backButton.isClick(button)){
			Panel.setMenu(MenuMultiPlayerOption.getInstance());
			clear();
		}
		if (joinRoom.isClick(button)) {
			Panel.setMenu(MenuMultiPlayerRoomJoin.getInstance());
			clear();
		}
		
		if (createRoom.isClick(button)) {
			Panel.setMenu(MenuMultiPlayerRoomCreate.getInstance());
			clear();
		}
	}

	@Override
	public void keyAction(int keycode) {
		
	}

}
