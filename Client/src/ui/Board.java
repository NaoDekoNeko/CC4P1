package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.io.IOException;
import java.nio.ByteBuffer;

import model.Apple;
import model.Snake;
import model.SnakeSet;
import model.Tile;
import model.TileGrid;
import service.Client;
import util.TileType;

public class Board extends MenuWidget {
	private static TileGrid grid;
	private static Snake player;
	private static boolean connected;
	private static Board instance;
	private static int apples = 100;
	private static long startedAt = System.currentTimeMillis();
	private static Panel root;
	public static int level = 0;
	public static int applesEaten = 0;

	private Board(Panel root) {
		super(root, 0, 0);
		Board.root = root;
	}

	public static void init(Panel root) {
		if (instance == null) {
			instance = new Board(root);
		}
	}

	public static void setSizeBoard(int columns, int rows) {
		Board.grid = new TileGrid(columns, rows,
				Math.min(Board.root.getWidth() / columns, Board.root.getHeight() / rows));
		Board.grid.popFreeSetElement(TileGrid.getTile(0, 0));
		Board.player = new Snake(0, 0, Color.BLUE, Color.GREEN);
		SnakeSet.addSnake(Board.player);
		Board.connected = false;
	}

	public static void setSizeGrid(int columns, int rows) {
		Board.grid = new TileGrid(columns, rows,
				Math.min(Board.root.getWidth() / columns, Board.root.getHeight() / rows));
		Board.grid.popFreeSetElement(TileGrid.getTile(0, 0));
	}

	public static Board getInstance() {

		if (instance == null) {
			throw new AssertionError("You have to call init first");
		}

		return instance;
	}

	public static boolean isConnected() {
		return Board.connected;
	}

	@Override
	public void update() {
		Board.apples = MenuSetting.getWidgets().get(0).getEnteredNumber();

		if (Board.connected) {
			ByteBuffer res = ByteBuffer.wrap(Client.sendMessage("GET UPDATE_BOARD"));
			int cnt = 0;
			while (true) {
				if (res.get((cnt) * 8 + 7) != (byte) 1) {
					break;
				}
				TileGrid.getTile(res.get(cnt * 8 + 1) & 0xFF, res.get(cnt * 8 + 2) & 0xFF)
						.setType(TileType.values()[res.get(cnt * 8)], new Color(res.getInt(cnt * 8 + 3)));
				cnt++;
			}

			return;
		}

		Apple.generate(Board.apples);
		SnakeSet.calculateSnakes();

		if (System.currentTimeMillis() - startedAt < 1000 / MenuSetting.getWidgets().get(3).getEnteredNumber()) {
			return;
		}

		startedAt = System.currentTimeMillis();

		SnakeSet.moveSnakes();
		SnakeSet.checkSnake();
	}

	public void draw(Graphics g) {
		int spaceWidth = (this.getRoot().getWidth() - TileGrid.getColumns() * TileGrid.getUnitSize()) / 2;
		int spaceHeight = (this.getRoot().getHeight() - TileGrid.getRows() * TileGrid.getUnitSize()) / 2;

		for (int x = 0; x < TileGrid.getColumns(); x++) {
			for (int y = 0; y < TileGrid.getRows(); y++) {
				Tile tile = TileGrid.getTile(x, y);
				g.setColor(tile.getColor());
				g.fillRect(tile.getStepX() + spaceWidth, tile.getStepY() + spaceHeight, tile.getSize()[0] - 1,
						tile.getSize()[1] - 1);
			}

		}

		for (Snake snake : SnakeSet.getSnakeList()) {
			if (snake.getName() == null) {
				continue;
			}
			g.setColor(Color.WHITE);
			g.setFont(new Font("Ink Free", Font.BOLD, 15));
			g.drawString(snake.getName(), snake.getHead().getStepX() + 10, snake.getHead().getStepY() + 10);
		}
	}

	public static TileGrid getGrid() {
		return Board.grid;
	}

	public static void reset() {
		if (Board.connected) {
			Board.connected = false;
			try {
				Client.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		Apple.reset();
		SnakeSet.reset();
		TileGrid.reset();
		Board.grid.resetFreeSet();
		Board.level = 0;
		Board.applesEaten = 0;
	}

	public static Snake getPlayerSnake() {
		return Board.player;
	}

	public static int connectServer(String host, int port) {
		if (Client.start(host, port) == null) {
			return -1;
		}
		Board.connected = true;
		return 0;
	}

	public static void setapples(int n) {
		Board.apples = n;
	}

	public String toString() {
		return "this is board";
	}

}
