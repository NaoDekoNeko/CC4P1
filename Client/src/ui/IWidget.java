package ui;

import java.awt.*;

public interface IWidget {
	public void setColor(Color colorDefault, Color colorOnHover, Color colorOnClick);

	public void setColor(Color colorTitle, Color colorText);

	public void setText(String text);

	public void draw(Graphics g);

	public void update();

	public boolean isClick(int button);

	public void getNumber(int keycode);

	public void getString(int keycode);

	public boolean isEntered();

	public int getEnteredNumber();
	
	public String getEnteredString();
}
