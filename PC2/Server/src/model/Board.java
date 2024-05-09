package model;

import service.Server;

public class Board {
	private TileGrid grid;
	private int delay;
	private SnakeSet snakeSet;
	private Apple apple;
	private long startedAt;
	public static int level = 0;
	public static int applesEaten = 0;

	public Board(int columns, int rows, int unitSize, int numberApple) {
		this.grid = new TileGrid(columns, rows, unitSize);
		this.delay = 0;
		this.apple = new Apple(grid);
		this.apple.setNumberApples(numberApple);
		this.snakeSet = new SnakeSet(grid, this.apple);

	}

	public void setDelay(int delay) {
		this.delay = delay;
	}

	public byte[] getUpdateGrid(Tile[][] dstTile) {
		byte[] result = new byte[Server.getLenBuffer()];
		int cnt = 0;
		for (int x = 0; x < grid.getColumns(); x++)
			for (int y = 0; y < grid.getRows(); y++) {
				if (dstTile[x][y].getType() != grid.getTile(x, y).getType()) {
				} else if (dstTile[x][y].getColor() != grid.getTile(x, y).getColor()) {
				} else {
					continue;
				}
				copyBytes(result, getUpdateTile(dstTile[x][y], grid.getTile(x, y)), (++cnt - 1) * 8, 8);

			}
		return result;
	}

	public int[] getSizeBoard() {
		int[] result = new int[2];
		result[0] = this.grid.getColumns();
		result[1] = this.grid.getRows();
		return result;
	}

	private byte[] getUpdateTile(Tile dstTile, Tile srcTile) {
		byte[] result = new byte[8];
		int argb = srcTile.getColor().getRGB();
		dstTile.setType(srcTile.getType(), srcTile.getColor());

		result[0] = (byte) srcTile.getType().ordinal();
		result[1] = (byte) srcTile.getIndexX();
		result[2] = (byte) srcTile.getIndexY();
		result[3] = (byte) (argb >> 24);
		result[4] = (byte) (argb >> 16);
		result[5] = (byte) (argb >> 8);
		result[6] = (byte) (argb);
		result[7] = (byte) 1;
		return result;

	}

	private static void copyBytes(byte[] dst, byte[] src, int start, int size) {
		for (int i = 0; i < size; i++) {
			dst[start + i] = src[i];
		}
	}

	public Snake addRandomSnake() {
		return snakeSet.addRandomSnake();
	}

	public void update() {
		long curr = System.currentTimeMillis();
		if (curr - startedAt >= delay) {
			apple.generate();
			snakeSet.calculateSnakes();
			snakeSet.moveSnakes();
			snakeSet.checkSnake();
			startedAt = curr;
		}

	}

	public TileGrid getGrid() {
		return this.grid;
	}

	public SnakeSet getSnakeList() {
		return this.snakeSet;
	}

	public int countSnake() {
		return snakeSet.getSnakeList().size();
	}

	public synchronized void reset() {
		apple.reset();
		snakeSet.reset();
		grid.reset();

	}

}
