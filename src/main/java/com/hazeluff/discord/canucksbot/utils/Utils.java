package com.hazeluff.discord.canucksbot.utils;

import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {
	private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

	/**
	 * Invokes Thread.sleep() and catches the exception.
	 * 
	 * @param duration
	 *            duration in ms to sleep
	 */
	public static void sleep(long duration) {
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			LOGGER.error("Could not sleep for [" + duration + "]", e);
		}
	}

	/**
	 * Gets a random element from the provided list.
	 * 
	 * @param list
	 *            list to get random element from
	 * @return random element from list
	 */
	public static <T> T getRandom(List<T> list) {
		return list.get(new Random().nextInt(list.size()));
	}

	/**
	 * Gets the current epoch time in ms.
	 * 
	 * @return current epoch time in ms
	 */
	public static long getCurrentTime() {
		return System.currentTimeMillis();
	}
}
