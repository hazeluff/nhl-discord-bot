package com.hazeluff.discord.canucksbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.canucksbot.nhl.NHLGameScheduler;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.util.DiscordException;


public class CanucksBot {
	private static final Logger LOGGER = LoggerFactory.getLogger(CanucksBot.class);

	private final IDiscordClient client;
	private final NHLGameScheduler nhlGameScheduler;

	public CanucksBot(String botToken) {
		client = getClient(botToken);
		nhlGameScheduler = new NHLGameScheduler(client);

		EventDispatcher dispatcher = client.getDispatcher();
		dispatcher.registerListener(new ReadyListener(client, nhlGameScheduler));
		dispatcher.registerListener(new CommandListener(client, nhlGameScheduler));
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
