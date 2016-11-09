package com.hazeluff.discord.canucksbot;

/**
 * Hello world!
 *
 */
public class BotRunner 
{
	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
		CanucksBot bot = new CanucksBot(args[0]);
    }
}
