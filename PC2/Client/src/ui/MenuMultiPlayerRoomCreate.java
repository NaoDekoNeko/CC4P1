package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;

import service.Client;

public class MenuMultiPlayerRoomCreate extends Menu {
	private static MenuMultiPlayerRoomCreate instance;
	private static MenuWidget widthBoard, speedBoard, createButton,heightBoard, appleNumber;
	private static ArrayList<MenuWidget> widgets;
	
	private MenuMultiPlayerRoomCreate() {
		super();
	}
	
	
	public static void init(Panel root) {
		if (instance == null) {
			instance = new MenuMultiPlayerRoomCreate();
			widgets = new ArrayList<>();
			
			
			widthBoard = new MenuEntry(root, "Width: ", "100", new Font("Courier New", Font.BOLD, 20),
					300, 350);
			
			heightBoard = new MenuEntry(root, "Height: ", "70", new Font("Courier New", Font.BOLD, 20),
					300, 400);
		

			speedBoard = new MenuEntry(root, "Speed: ", "30", new Font("Courier New", Font.BOLD, 20),
					300, 450);
			
			appleNumber = new MenuEntry(root, "Number of Apples: ", "50", new Font("Courier New", Font.BOLD, 20),
					300, 500);
			
			createButton =  new MenuString(root, "Create", new Font("Courier New", Font.BOLD, 30), MenuString.Side.CENTER, 600);
			
			widthBoard.setColor(Color.YELLOW, Color.WHITE, Color.CYAN, Color.BLUE);
			heightBoard.setColor(Color.YELLOW, Color.WHITE, Color.CYAN, Color.BLUE);
			speedBoard.setColor(Color.YELLOW, Color.WHITE, Color.CYAN, Color.BLUE);
			appleNumber.setColor(Color.YELLOW, Color.WHITE, Color.CYAN, Color.BLUE);
			createButton.setColor( Color.WHITE, Color.CYAN, Color.BLUE);

			widgets.add(widthBoard);
			widgets.add(speedBoard);
			widgets.add(appleNumber);
			widgets.add(createButton);
			widgets.add(heightBoard);
			
			
		}
	}
	
	
	public static MenuMultiPlayerRoomCreate getInstance() {
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
		if (widthBoard.isClick(button)) {
			
		}
		
		if (heightBoard.isClick(button)) {
			
		}
		
		
		if (speedBoard.isClick(button)) {
			
		}
		
		if (appleNumber.isClick(button)) {
			
		}
		
		if (createButton.isClick(button)) {
			clear();
			byte[] buffer = Client.sendMessage("PUT CREATE_BOARD"+" "+widthBoard.getEnteredString()+" "+heightBoard.getEnteredString() + " "+ speedBoard.getEnteredString() + " " + appleNumber.getEnteredString());
			Board.setSizeGrid(widthBoard.getEnteredNumber(), heightBoard.getEnteredNumber());
			Panel.setMenu(MenuMultiPlayer.getInstance());
			MenuMultiPlayer.setStringInfo("ID: " + new String(buffer).trim());
		}
		
	}

	@Override
	public void keyAction(int keycode) {
		if (widthBoard.isClicked()) {
			widthBoard.getNumber(keycode);
		}
		
		if (heightBoard.isClicked()) {
			heightBoard.getNumber(keycode);
		}
		
		
		if (speedBoard.isClicked()) {
			speedBoard.getNumber(keycode);
		}
		
		if (appleNumber.isClicked()) {
			appleNumber.getNumber(keycode);
		}
	}

}


