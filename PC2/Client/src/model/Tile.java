package model;

import util.TileType;
import java.awt.Color;


public class Tile {
	private TileType type;
	private Color color;
	private int[] index;
	private int[] size;
	
	
	Tile(int[] index, int[] size){
		this.type = TileType.BACKGROUND;
		this.color = Color.BLACK;
		this.index = index;
		this.size = size;
	}
	
	public void setSize(int[] size) {
		this.size = size;
	}
	
	public int[] getSize(){
		return this.size;
	}
	
	public void setIndex(int[] index) {
		this.index = index;
	}
	
	public int[] getIndex(){
		return this.index;
	}
	
	public void setColor(Color color) {
		this.color = color;
	}
	
	public Color getColor() {
		return this.color;
	}
	
	public TileType getType() {
		return type;
	}
	
	public void setType(TileType type) {
		this.type = type;
		this.color = Color.BLACK;
	}
	
	public void setType(TileType type, Color color) {
		this.type = type;
		this.color = color;
	}
	
	public String toString() {
		return String.format("[x=%d,y=%d]",this.index[0],this.index[1] );
	}
	
	public int getIndexX() {
		return index[0];
	}
	
	public int getIndexY() {
		return index[1];
	}
	
	public int getStepX() {
		return index[0]*size[0];
	}
	
	public int getStepY() {
		return index[1]*size[1];
	}
	
	public void reset() {
		this.type = TileType.BACKGROUND;
		this.color = Color.BLACK;
	}
}
