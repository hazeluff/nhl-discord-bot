package com.hazeluff.discord.nhlbot.bot;

public class NHLBotException extends RuntimeException {
	public NHLBotException(RuntimeException e) {
		super(e);
	}

	private static final long serialVersionUID = -429202487421777493L;
}
