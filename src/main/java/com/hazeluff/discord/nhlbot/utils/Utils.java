package com.hazeluff.discord.nhlbot.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {
	private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

	private static final Random random = new Random();
	
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
	 * Gets a random int value.
	 * 
	 * @return random value
	 */
	public static int getRandomInt() {
		return random.nextInt();
	}
	
	/**
	 * Gets a random long value.
	 * @return random value
	 */
	public static long getRandomLong() {
		return random.nextLong();
	}

	/**
	 * Gets a random element from the provided list.
	 * 
	 * @param list
	 *            list to get random element from
	 * @return random element from list
	 */
	public static <T> T getRandom(List<T> list) {
		return list.get(random.nextInt(list.size()));
	}

	/**
	 * Gets a random enum value from an enum class.
	 * 
	 * @param enumClass
	 *            class to get random enum from
	 * @return random enum
	 */
	public static <T extends Enum<?>> T getRandom(Class<T> enumClass) {
		int x = random.nextInt(enumClass.getEnumConstants().length);
		return enumClass.getEnumConstants()[x];
	}

	/**
	 * Gets the current epoch time in ms.
	 * 
	 * @return current epoch time in ms
	 */
	public static long getCurrentTime() {
		return System.currentTimeMillis();
	}

	/**
	 * Gets the current date (UTC)
	 * 
	 * @return
	 */
	public static LocalDate getCurrentDate(ZoneId zone) {
		return LocalDate.now(zone);
	}

	/**
	 * Gets file name from the path of a file.
	 * 
	 * @param filePath
	 *            path of the file
	 * @return name of the file
	 */
	public static String getFileName(String filePath) {
		return filePath.substring(filePath.lastIndexOf("/") + 1);
	}

	/**
	 * Creates a set from a var arg of elements. Maintains order
	 * 
	 * @param elements
	 *            elements to create set with
	 * @return Set of elements
	 */
	@SafeVarargs
	public static <T> Set<T> asSet(T... elements) {
		return new LinkedHashSet<T>(Arrays.asList(elements));
	}
}
