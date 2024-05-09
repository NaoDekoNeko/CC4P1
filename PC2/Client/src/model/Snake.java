package model;

import java.awt.Color;
import java.util.LinkedList;

import ui.Board;
import util.SnakeDirection;
import util.TileType;

public class Snake {
	private LinkedList<Tile> body;
	private Tile head, nextHead;
	private Color colorBody, colorHead;
	private SnakeDirection direction;
	private boolean dead;
	private String name;

	public Snake(int x, int y, Color colorHead, Color colorBody) {
		this.body = new LinkedList<Tile>();
		this.head = TileGrid.getTile(x, y);
		this.colorBody = colorBody;
		this.colorHead = colorHead;
		this.direction = SnakeDirection.DOWN;
		this.dead = false;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getName(){
		return this.name;
	}

	public void changeDirection(SnakeDirection direction) {
		if (this.isDead())
			return;

		if (this.direction == SnakeDirection.DOWN && direction == SnakeDirection.UP) {

		}

		else if (this.direction == SnakeDirection.UP && direction == SnakeDirection.DOWN) {

		}

		else if (this.direction == SnakeDirection.LEFT && direction == SnakeDirection.RIGHT) {

		}

		else if (this.direction == SnakeDirection.RIGHT && direction == SnakeDirection.LEFT) {

		} else {
			this.direction = direction;
		}

	}

	public void calculateMoveStep() {
		if (this.isDead())
			return;

		int[] curHead = this.head.getIndex();

		switch (this.direction) {
			case UP:
				if (curHead[1] == 0) {
					this.nextHead = TileGrid.getTile(curHead[0], TileGrid.getRows() - 1);
					break;
				}
				this.nextHead = TileGrid.getTile(curHead[0], curHead[1] - 1);
				break;
			case DOWN:
				if (curHead[1] == TileGrid.getRows() - 1) {
					this.nextHead = TileGrid.getTile(curHead[0], 0);
					break;
				}
				this.nextHead = TileGrid.getTile(curHead[0], curHead[1] + 1);
				break;
			case LEFT:
				if (curHead[0] == 0) {
					this.nextHead = TileGrid.getTile(TileGrid.getColumns() - 1, curHead[1]);
					break;
				}
				this.nextHead = TileGrid.getTile(curHead[0] - 1, curHead[1]);
				break;
			case RIGHT:
				if (curHead[0] == TileGrid.getColumns() - 1) {
					this.nextHead = TileGrid.getTile(0, curHead[1]);
					break;
				}
				this.nextHead = TileGrid.getTile(curHead[0] + 1, curHead[1]);
				break;

			default:
				break;
		}
	}

	public void move() {
		if (this.isDead())
			return;
	
		this.body.addLast(this.head);
	
		if (this.nextHead.getType() == TileType.FOOD) {
			eatApple();
		}
	
		if (this.nextHead.getType() == TileType.BODY || this.nextHead.getType() == TileType.HEAD || this.nextHead.getType() == TileType.OBSTACLE) {
			this.dead();
			return;
		}
	
		this.body.getLast().setType(TileType.BODY, this.colorBody);
		this.body.getFirst().setType(TileType.BACKGROUND);
		this.body.removeFirst();
		this.head = this.nextHead;
		this.head.setType(TileType.HEAD, this.colorHead);
	}
	
	public void eatApple() {
		this.body.addFirst(this.nextHead);
		Apple.removeApple(this.nextHead);
		Board.applesEaten++;
		int scoreToBeat = 15;
		if (Board.applesEaten % scoreToBeat == 0) { // Every 10 apples eaten
			Board.level++;
			scoreToBeat = (int) (scoreToBeat * Math.pow(2, Board.level / 1.61));
			/*
			 * In a stoke of genius, I decided to use the golden ratio to calculate the score to beat
			 * Level 0: 15
			 * Level 1: 23
			 * Level 2: 53
			 * Level 3: 122
			 */
			Board.getGrid().generateMazeObstacles();
			if (Board.level > 3) { // Reset level after reaching 3
				Board.level = 0;
			}
		}
	}

	public Tile getHead() {
		return this.head;
	}

	public int getSizeBody() {
		return body.size();
	}

	private void dead() {
		this.dead = true;
		for (Tile tile : body) {
			Apple.addApple(tile);
		}
	}

	public boolean isDead() {
		return this.dead;
	}
}
