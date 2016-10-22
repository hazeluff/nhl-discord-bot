package com.hazeluff.discort.canucksbot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.util.DiscordException;


public class CanucksBot {
	private static final Logger LOGGER = LogManager.getLogger(CanucksBot.class);

	private IDiscordClient client;

	public CanucksBot() {
		client = getClient(Config.BOT_TOKEN);
		EventDispatcher dispatcher = client.getDispatcher();
		dispatcher.registerListener(new CommandListener(client));
	}

	public static IDiscordClient getClient(String token) {
		ClientBuilder clientBuilder = new ClientBuilder();
		clientBuilder.withToken(token);
		try {
			return clientBuilder.login();
		} catch (DiscordException e) {
			LOGGER.error(e);
			return null;
		}
	}
}
