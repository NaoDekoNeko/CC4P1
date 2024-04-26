package model;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;
import util.TileType;

public class Apple {
	private HashSet<Tile> appleSet = new HashSet<>();
	private TileGrid grid;
	private int numberApples;

	public Apple(TileGrid tileGrid) {
		this.grid = tileGrid;
	}

	public void setNumberApples(int num) {
		numberApples = num;
	}

	public void generate() {
		while (appleSet.size() < numberApples) {
			int appleX = (int) (Math.random() * (grid.getColumns() - 1));
			int appleY = (int) (Math.random() * (grid.getRows() - 1));
			if (grid.getTile(appleX, appleY).getType() == TileType.BACKGROUND) {
				this.addApple(grid.getTile(appleX, appleY));

			}
		}
	}

	public void removeApple(Tile apple) {
		appleSet.remove(apple);
	}

	public void addApple(Tile apple) {
		apple.setType(TileType.FOOD, Color.RED);
		appleSet.add(apple);
	}

	public Set<Tile> getApples() {
		return this.appleSet;
	}

	public void reset() {
		this.appleSet.clear();
	}
}
