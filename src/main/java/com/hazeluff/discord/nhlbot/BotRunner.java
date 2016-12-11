package com.hazeluff.discord.nhlbot;

import com.hazeluff.discord.nhlbot.bot.ExceptionHandler;
import com.hazeluff.discord.nhlbot.bot.NHLBot;

/**
 * Hello world!
 *
 */
public class BotRunner 
{
	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
		NHLBot bot = new NHLBot(args[0]);
    }
}
