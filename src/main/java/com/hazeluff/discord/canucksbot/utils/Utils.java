package com.hazeluff.discord.canucksbot.utils;

import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {
	private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

	public static void sleep(long duration) {
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			LOGGER.error("Could not sleep for [" + duration + "]", e);
		}
	}

	public static <T> T getRandom(List<T> list) {
		return list.get(new Random().nextInt(list.size()));
	}
}
