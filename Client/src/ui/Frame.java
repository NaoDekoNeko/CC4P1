package ui;

import javax.swing.JFrame;

public class Frame extends JFrame {
	private static final long serialVersionUID = 1L;
	private static Frame instance;
	private Panel panel;

	private Frame() {
		panel = Panel.getInstance();
		this.add(panel);
		this.setTitle("Snake");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
//		this.setUndecorated(true);
		this.setResizable(false);
		this.setVisible(true);
		this.pack();
		panel.startGame();
		
		
	}

	
	public static Frame getInstance() {
		if (instance == null) {
			instance = new Frame();
		}
		return instance;
	}

	public static void close() {
		System.exit(0); // stop program
		instance.dispose(); // close window
		instance.setVisible(false);
	}
	
	public static void setFullScreen() {
		instance.dispose();

		Panel.setFullScreen();

		if (Panel.isFullScreen()) {
			instance.setUndecorated(true);
		}
		else {
			instance.setUndecorated(false);
		}
		instance.pack();


		instance.setLocationRelativeTo(null);
		instance.setVisible(true);
//		instance.pack();
	}
}
