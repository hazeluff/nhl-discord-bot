package com.hazeluff.discord.canucksbot.utils;

import java.util.List;
import java.util.Random;

public class Utils {
	public static void sleep(long duration) {
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static <T> T getRandom(List<T> list) {
		return list.get(new Random().nextInt(list.size()));
	}
}
