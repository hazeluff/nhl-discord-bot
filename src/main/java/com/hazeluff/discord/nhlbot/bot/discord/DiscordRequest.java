package com.hazeluff.discord.nhlbot.bot.discord;

import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 * Functional Interface for a discord request.
 * 
 * @param <T>
 *            type of the return value
 */
@FunctionalInterface
public interface DiscordRequest<T> {
	/**
	 * This is called when the request is attempted.
	 *
	 * @return The result of this request, if any.
	 *
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 */
	T perform() throws DiscordException, MissingPermissionsException, RateLimitException;
}
