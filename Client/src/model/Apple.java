package model;

import java.awt.Color;
import java.util.HashSet;
import util.TileType;

public class Apple {
	private static HashSet<Tile> apples = new HashSet<Tile>();
	
	
	public static void generate(int num) {
		while(apples.size() < num) {
			int appleX = (int)(Math.random()*(TileGrid.getColumns() - 1));
			int appleY = (int)(Math.random()*(TileGrid.getRows() - 1));
			if (TileGrid.getTile(appleX,appleY).getType() == TileType.BACKGROUND){
				Apple.addApple(TileGrid.getTile(appleX,appleY));
				
			
			}
		}
	}
	
	public static void removeApple(Tile apple) {
		apples.remove(apple);
	}
	
	public static void addApple(Tile apple) {
		apple.setType(TileType.FOOD, Color.RED);
		apples.add(apple);
	}
	
	public static HashSet<Tile> getApples(){
		return Apple.apples;
	}
	
	public static void reset() {
		Apple.apples.clear();
	}
}
