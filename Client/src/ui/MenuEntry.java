package ui;

import java.awt.*;

public class MenuEntry extends MenuWidget {
	private String title;
	private String text;
	private Color colorOnHover = Color.WHITE, colorOnClick = Color.WHITE;
	private Font font;
	private Color colorTitle = Color.WHITE, colorText = Color.WHITE, colorCurrentTitle = Color.WHITE,
			colorCurrentText = Color.WHITE;
	private int[] size;
	private boolean clicked = false;
	private boolean entered = false;

	MenuEntry(Panel root, String title, Font font, int x, int y) {
		super(root, x, y);
		this.title = title;
		this.text = "";
		this.font = font;
		size = new int[] { 0, 0 };
	}

	MenuEntry(Panel root, String title, String text, Font font, int x, int y) {
		super(root, x, y);
		this.title = title;
		this.text = text;
		this.font = font;
		size = new int[] { 0, 0 };
	}

	public void getChar(char c) {
		this.text += c;
	}

	public boolean isEntered() {
		return this.entered;
	}

	public int getEnteredNumber() {
		this.entered = false;
		this.clicked = false;
		int result = Integer.parseInt(this.text);
		return result;
	}

	public String getEnteredString() {
		this.entered = false;
		this.clicked = false;
		return this.text;
	}

	public void getNumber(int keycode) {

		if (keycode == 10 && this.text.length() > 0) {
			this.entered = true;
			this.clicked = false;
			return;
		}

		if (keycode == 8 && this.text.length() > 0) {
			this.text = this.text.substring(0, this.text.length() - 1);
		}
		if (keycode <= 57 && keycode >= 48)
			this.text += (char) keycode;
	}

	public void getString(int keycode){
		if (keycode == 10 && this.text.length() > 0) {
			this.entered = true;
			this.clicked = false;
			return;
		}

		if (keycode == 8 && this.text.length() > 0) {
			this.text = this.text.substring(0, this.text.length() - 1);
			return;
		}

		this.text += (char) keycode;
	}

	public void draw(Graphics g) {

		g.setFont(this.font);
		size[0] = g.getFontMetrics(this.font).stringWidth(this.text + this.title);
		size[1] = (int) (g.getFontMetrics(g.getFont()).getHeight() * 0.65);

		g.setColor(this.colorCurrentTitle);
		g.drawString(this.title, this.getX(), this.getY());

		g.setColor(this.colorCurrentText);
		g.drawString(this.text, this.getX() + g.getFontMetrics(this.font).stringWidth(this.title), this.getY());

	}

	public void setColor(Color colorTitle, Color colorText, Color colorOnHover, Color colorOnClick) {
		this.colorOnHover = colorOnHover;
		this.colorOnClick = colorOnClick;
		this.colorCurrentTitle = colorTitle;
		this.colorCurrentText = colorText;
		this.colorText = colorText;
		this.colorTitle = colorTitle;
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

	private void onHover() {

		if (isHover()) {
			this.colorCurrentTitle = this.colorOnHover;
			this.colorCurrentText = this.colorOnHover;
		} else {
			this.colorCurrentTitle = this.colorTitle;
			this.colorCurrentText = this.colorText;
		}
	}

	public boolean isClick(int button) {

		if (isHover() && button == 1) {
			this.entered = false;
			this.clicked = true;
			this.text = "";
			return true;

		}

		if (!isHover() && button == 1) {
			this.clicked = false;
		}

		return false;

	}

	public boolean isClicked() {
		return this.clicked;
	}

	private void onClick() {
		if (this.clicked) {
			this.colorCurrentTitle = this.colorOnClick;
			this.colorCurrentText = this.colorText;
		} else if (!isHover()) {
			this.colorCurrentTitle = this.colorTitle;
			this.colorCurrentText = this.colorText;
			this.clicked = false;
		}
		// this.clicked = false;
	}

	public enum Side {
		CENTER, DEFAULT, N;
	}
}
