package model;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class SnakeSet {
	private static HashSet<Snake> snakes = new HashSet<>();
	
	private SnakeSet() {
		
	}
	
	public static void addSnake(Snake snake) {
		snakes.add(snake);
	}
	
	public static int countSnake() {
		return snakes.size();
	}
	
	public static void removeSnake(Snake snake) {
		snakes.remove(snake);
	}
	
	public static Set<Snake> getSnakeList(){
		return  SnakeSet.snakes;
	}
	
	public static void moveSnakes() {
		for(Snake snake:snakes) {
			snake.move();
		}
	}
	
	public static void calculateSnakes() {
		for(Snake snake:snakes) {
			snake.calculateMoveStep();
		}
	}
	
	public static void checkSnake() {		
		for (Iterator<Snake> i = snakes.iterator(); i.hasNext();) {
		    Snake element = i.next();
		    if (element.isDead()) {
		        i.remove();
		    }
		}
	}
	
	public static void reset() {
		SnakeSet.snakes.clear();
	}
}
