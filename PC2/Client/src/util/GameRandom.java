package util;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;

public class GameRandom {
	private static Random random = new Random();

	public static <E> E getRandomElement(Set<? extends E> set) {

		int randomNumber = random.nextInt(set.size());
		Iterator<? extends E> iterator = set.iterator();
		int currentIndex = 0;
		E randomElement = null;
		while (iterator.hasNext()) {
			randomElement = iterator.next();
			if (currentIndex == randomNumber)
				return randomElement;
			currentIndex++;
		}
		return randomElement;
	}

}
