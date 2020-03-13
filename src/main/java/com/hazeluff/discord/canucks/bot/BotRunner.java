package com.hazeluff.discord.canucks.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.canucks.Config;
import com.hazeluff.discord.canucks.nhl.GameScheduler;

/**
 * Hello world!
 *
 */
public class BotRunner {
	private static final Logger LOGGER = LoggerFactory.getLogger(BotRunner.class);

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(BotRunner.class));

		GameScheduler gameScheduler = new GameScheduler();

		if (Config.Debug.isLoadGames()) {
			LOGGER.info("Loading the games...");
			gameScheduler.start();
		} else {
			gameScheduler.setInit(true);
		}

		CanucksBot bot = CanucksBot.create(gameScheduler, args[0]);
    }
}
