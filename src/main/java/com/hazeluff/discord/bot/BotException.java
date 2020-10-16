package com.hazeluff.discord.bot;

public class BotException extends RuntimeException {
	public BotException(RuntimeException e) {
		super(e);
	}

	public BotException(String string) {
		super(string);
	}

	private static final long serialVersionUID = -429202487421777493L;
}
