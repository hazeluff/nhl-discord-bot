package com.hazeluff.discord.nhlbot.bot;

import sx.blah.discord.util.DiscordException;

public class NHLBotException extends RuntimeException {
	public NHLBotException(DiscordException e) {
		super(e);
	}

	private static final long serialVersionUID = -429202487421777493L;
}
