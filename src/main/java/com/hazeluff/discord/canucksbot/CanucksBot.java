package com.hazeluff.discord.canucksbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.util.DiscordException;


public class CanucksBot {
	private static final Logger LOGGER = LoggerFactory.getLogger(CanucksBot.class);

	private static IDiscordClient client;

	public CanucksBot(String botToken) {
		client = getClient(botToken);
		EventDispatcher dispatcher = client.getDispatcher();
		dispatcher.registerListener(new ReadyListener(client));
		dispatcher.registerListener(new CommandListener(client));
	}

	public static IDiscordClient getClient(String token) {
		ClientBuilder clientBuilder = new ClientBuilder();
		clientBuilder.withToken(token);
		try {
			return clientBuilder.login();
		} catch (DiscordException e) {
			LOGGER.error("Could not login.", e);
			return null;
		}
	}
}
