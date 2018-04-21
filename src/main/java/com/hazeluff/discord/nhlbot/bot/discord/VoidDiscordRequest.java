package com.hazeluff.discord.nhlbot.bot.discord;

import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 * Functional Interface for a void discord request.
 */
@FunctionalInterface
public interface VoidDiscordRequest extends DiscordRequest<Object> {

	default Object perform() throws DiscordException, MissingPermissionsException, RateLimitException {
		doPerform();
		return null;
	}

	/**
	 * This is called when the request is attempted.
	 *
	 * @throws DiscordException
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 */
	void doPerform() throws DiscordException, MissingPermissionsException, RateLimitException;
}