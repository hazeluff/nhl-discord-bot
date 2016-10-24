package com.hazeluff.discord.canucksbot.utils;

public class ThreadUtils {
	public static void sleep(long duration) {
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
