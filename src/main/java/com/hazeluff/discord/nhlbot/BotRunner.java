package com.hazeluff.discord.nhlbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.bot.ExceptionHandler;
import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.nhl.GameScheduler;

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

		NHLBot bot = NHLBot.create(gameScheduler, args[0]);
    }
}
