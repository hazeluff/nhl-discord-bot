package com.hazeluff.discord.nhlbot.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
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
			LOGGER.error("Sleep interupted");
		}
	}

	/**
	 * If the string is longer than the given length, it is shortened and has "..."
	 * appended.
	 * 
	 * @return
	 */
	public static String shorten(String s, int length) {
		if(s.length() > length) {
			s = s.substring(0, length) + "...";
		}
		return s;
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
	
	public static <T> List<T> getRandomList(List<T> sourceList, int numberOfElements) {
		List<T> copiedList = new ArrayList<>(sourceList);
		List<T> randomList = new ArrayList<>();
		for (int i = 0; i < numberOfElements; i++) {
			int indexToRemove = random.nextInt(copiedList.size());
			randomList.add(copiedList.remove(indexToRemove));
		}
		return randomList;
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

	public static <T> T getAndRetry(CheckedSupplier<T> supplier, int retries, long sleepMs, String description) {
		for (int tries = 0; tries < retries; tries++) {
			try {
				return supplier.get();
			} catch (Exception e) {
				LOGGER.warn(String.format("Failed to get [%s]. Retry in [%sms]", description, sleepMs), e);
				Utils.sleep(sleepMs);
			}
		}
		throw new TimeoutException(String.format("Failed to get [%s] after retries [%s]", description, retries));
	}

	public static <T> boolean isListEquivalent(List<T> listA, List<T> listB) {
		return listA.containsAll(listB) && listB.containsAll(listA) && listA.size() == listB.size();
	}
}
