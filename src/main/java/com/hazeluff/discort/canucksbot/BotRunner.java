package com.hazeluff.discort.canucksbot;

import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 * Hello world!
 *
 */
public class BotRunner 
{
	public static void main(String[] args) throws DiscordException, MissingPermissionsException, RateLimitException
    {
		CanucksBot bot = new CanucksBot();
    }


}
