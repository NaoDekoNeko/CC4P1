package ui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

// import service.Client;
// import util.*;

public class Panel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private static Timer timer;
	private static Menu menu;
	private static long startedAt = System.currentTimeMillis();
	private static int countFrame = 0;
	private static MenuWidget fpsInfo;
	private static boolean fullScreen = false;
	private static Panel instance;
	private Panel() {
		this.setPreferredSize(new Dimension(Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT));
//		this.setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
		this.setBackground(Color.black);
		this.setFocusable(true);
		this.addKeyListener(new MyKeyAdapter());
		this.addMouseListener(new MyMouseAdapter());
		
		
		MenuSetting.init(this);
		Board.init(this);
		MenuStart.init(this);
		MenuSinglePlayer.init(this);
		MenuLoseSinglePlayer.init(this);
		MenuMultiPlayer.init(this);
		MenuMultiPlayerUser.init(this);
		MenuMultiPlayerOption.init(this);
		MenuMultiPlayerRoomOption.init(this);
		MenuMultiPlayerRoomCreate.init(this);
		MenuMultiPlayerRoomJoin.init(this);
		MenuCreateAccount.init(this);
		MenuLoginAccount.init(this);
	

	}

	public void startGame() {
		Panel.timer.start();
	}

	public static void setMenu(Menu menu) {
		Panel.menu = menu;
	}
	
	public static void setFullScreen() {
		if (!fullScreen) {
			instance.setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
			fullScreen = true;
		}
		else {
			instance.setPreferredSize(new Dimension(Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT));
			fullScreen = false;
		}
	}
	
	public static boolean isFullScreen() {
		return fullScreen;
	}
	
	public static Panel getInstance() {
		if (instance == null) {
			instance = new Panel();
		}
		fpsInfo = new MenuString(instance, "0", new Font("Courier New", Font.BOLD, 15), MenuString.Side.L, MenuString.Side.L);
		fpsInfo.setColor(Color.GREEN, Color.GREEN, Color.GREEN);

		Panel.menu = MenuStart.getInstance();
		Panel.timer = new Timer(15, instance);
		return instance;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		draw(g);
		fpsInfo.draw(g);
		countFrame++;
		long t = System.currentTimeMillis();
		if (t - startedAt >= 1000) {
			fpsInfo.setText(Integer.toString(countFrame));
			countFrame = 0;
			startedAt = t;
		}

	}

	public void draw(Graphics g) {
		Panel.menu.draw(g);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Panel.menu.update();
		repaint();
	}

	public class MyKeyAdapter extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			Panel.menu.keyAction(e.getKeyCode());
		}

	}

	public class MyMouseAdapter extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			Panel.menu.mouseAction(e.getButton());

		}
	}

}
