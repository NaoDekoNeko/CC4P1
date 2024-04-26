package ui;

import java.awt.*;

public class MenuString extends MenuWidget {
	private String text;
	private Color colorDefault = Color.WHITE, colorOnHover = Color.WHITE, colorOnClick = Color.WHITE,
			colorCurrent = Color.WHITE;
	private Font font;
	private MenuString.Side side = MenuString.Side.DEFAULT, xSide = MenuString.Side.DEFAULT,
			ySide = MenuString.Side.DEFAULT;
	private int[] size = new int[] {0,0};
	private boolean clicked = false;
	
	MenuString(Panel root, String text, Font font, int x, int y) {
		super(root, x, y);
		this.text = text;
		this.font = font;
	}

	MenuString(Panel root, String text, Font font, MenuString.Side side) {
		super(root, -1, -1);
		this.text = text;
		this.font = font;
		this.side = side;

	}

	MenuString(Panel root, String text, Font font, MenuString.Side xSide, int y) {
		super(root, -1, y);
		this.text = text;
		this.font = font;
		this.xSide = xSide;
	}

	MenuString(Panel root, String text, Font font, int x, MenuString.Side ySide) {
		super(root, x, -1);
		this.text = text;
		this.font = font;
		this.ySide = ySide;
	}

	MenuString(Panel root, String text, Font font, MenuString.Side xSide, MenuString.Side ySide) {
		super(root, -1, -1);
		this.text = text;
		this.font = font;
		this.xSide = xSide;
		this.ySide = ySide;
	}

	public void setColor(Color colorDefault, Color colorOnHover, Color colorOnClick) {
		this.colorDefault = colorDefault;
		this.colorOnHover = colorOnHover;
		this.colorOnClick = colorOnClick;
		this.colorCurrent = colorDefault;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void update() {
		onHover();
		onClick();
	}

	private boolean isHover() {
		if (MouseInfo.getPointerInfo().getLocation().x - this.getRoot().getLocationOnScreen().x < this.getX()) {
			return false;
		}

		if (MouseInfo.getPointerInfo().getLocation().x - this.getRoot().getLocationOnScreen().x > this.getX()
				+ size[0]) {
			return false;
		}

		if (MouseInfo.getPointerInfo().getLocation().y - this.getRoot().getLocationOnScreen().y < this.getY()
				- size[1]) {
			return false;
		}

		if (MouseInfo.getPointerInfo().getLocation().y - this.getRoot().getLocationOnScreen().y > this.getY()) {
			return false;
		}

		return true;
	}

	public void onHover() {

		if (isHover()) {
			this.colorCurrent = this.colorOnHover;
		} else {
			this.colorCurrent = this.colorDefault;
		}
	}

	public boolean isClick(int button) {

		if (isHover() && button == 1) {
			this.clicked = true;
			return true;

		}
		this.clicked = false;
		return false;

	}

	public void onClick() {
		if (this.clicked) {
			this.colorCurrent = this.colorOnClick;
		} else if (!isHover()) {
			this.colorCurrent = this.colorDefault;
		}
		this.clicked = false;
	}

	public void draw(Graphics g) {
		g.setColor(this.colorCurrent);
		g.setFont(this.font);
		size[0] = g.getFontMetrics(this.font).stringWidth(this.text);
		size[1] = (int) (g.getFontMetrics(g.getFont()).getHeight() * 0.65);

		switch (this.xSide) {
			case CENTER:
				this.setX((getRoot().getWidth() - size[0]) / 2);
				break;

			case L:
				this.setX(0);
				break;

			case R:
				this.setX((getRoot().getWidth() - size[0]));
				break;

			default:
				break;
		}

		switch (this.ySide) {
			case CENTER:
				this.setY((getRoot().getHeight()) / 2);
				break;

			case L:
				this.setY(size[1]);
				break;

			case R:
				this.setX((getRoot().getHeight() - size[1]));
				break;

			default:
				break;
		}

		switch (this.side) {
			case CENTER:
				this.setX((getRoot().getWidth() - size[0]) / 2);
				this.setY((getRoot().getHeight()) / 2);
				break;

			default:
				break;

		}

		g.drawString(this.text, this.getX(), this.getY());

	}

	public enum Side {
		CENTER, L, R, DEFAULT;
	}

	public String toString() {
		return this.text;
	}

}
