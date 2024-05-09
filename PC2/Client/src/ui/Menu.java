package ui;

import java.awt.Graphics;

public abstract class Menu {

	Menu() {	}

	public abstract void update();

	public abstract void draw(Graphics g);

	public abstract void clear();

	public abstract void mouseAction(int button);

	public abstract void keyAction(int keycode);

}
