package model;

import java.awt.Color;
import java.util.HashSet;
import java.util.Iterator;

import util.GameRandom;
import util.TileType;

public class SnakeSet {
	private HashSet<Snake> snakes = new HashSet<Snake>();
	private TileGrid grid;
	private Apple apple;

	public SnakeSet(TileGrid grid, Apple apple) {
		this.grid = grid;
		this.apple = apple;
	}

	public void addSnake(Snake snake) {
		snakes.add(snake);
	}

	public Snake addRandomSnake() {
		Tile tile;
		do {
			tile = GameRandom.getRandomElement(grid.getTilesSet());
		} while (tile.getType() != TileType.BACKGROUND);
		Snake snake = new Snake(tile, grid, apple, new Color((int) (Math.random() * 0x1000000)),
				new Color((int) (Math.random() * 0x1000000)));
		this.addSnake(snake);
		return snake;
	}

	public void removeSnake(Snake snake) {
		snakes.remove(snake);
	}

	public HashSet<Snake> getSnakeList() {
		return this.snakes;
	}

	public void moveSnakes() {
		for (Snake snake : snakes) {
			snake.move();
		}
	}

	public void calculateSnakes() {
		for (Snake snake : snakes) {
			snake.calculateMoveStep();
		}
	}

	public void checkSnake() {
		for (Iterator<Snake> i = snakes.iterator(); i.hasNext();) {
			Snake element = i.next();
			if (element.isDead()) {
				i.remove();
			}
		}
	}

	public void reset() {
		this.snakes.clear();
	}
}
