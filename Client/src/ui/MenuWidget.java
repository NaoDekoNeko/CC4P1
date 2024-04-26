package ui;

import java.awt.Color;
import java.awt.Graphics;

public class MenuWidget implements IWidget {
	private Panel root;
	private int[] position;
	private boolean disabled = false;

	MenuWidget(Panel root, int x, int y) {
		this.root = root;
		position = new int[] { x, y };
	}

	public void active() {
		this.disabled = false;
	}

	public void disable() {
		this.disabled = true;
	}

	protected int getX() {
		return position[0];
	}

	protected int getY() {
		return position[1];
	}

	protected void setX(int x) {
		this.position[0] = x;
	}

	protected void setY(int y) {
		this.position[1] = y;
	}

	public boolean isDisable() {
		return this.disabled;
	}

	protected Panel getRoot() {
		return this.root;
	}

	@Override
	public void setColor(Color colorDefault, Color colorOnHover, Color colorOnClick) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setText(String text) {
		// TODO Auto-generated method stub

	}

	@Override
	public void draw(Graphics g) {
		// TODO Auto-generated method stub

	}

	@Override
	public void update() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isClick(int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void getNumber(int keycode) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isEntered() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getEnteredNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean isClicked() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setColor(Color cyan, Color white, Color yellow, Color cyan2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setColor(Color colorTitle, Color colorText) {
		// TODO Auto-generated method stub

	}

	@Override
	public void getString(int keycode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getEnteredString() {
		// TODO Auto-generated method stub
		return null;
	}
}
