package com.hazeluff.discord.nhlbot.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.Config;
import com.hazeluff.discord.nhlbot.nhl.GameScheduler;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.util.DiscordException;


public class NHLBot {
	private static final Logger LOGGER = LoggerFactory.getLogger(NHLBot.class);

	private final IDiscordClient client;
	private final DiscordManager discordManager;
	private final GameScheduler gameScheduler;
	private final String id;

	public NHLBot(String botToken) {
		LOGGER.info("Running CanucksBot v" + Config.VERSION);
		client = getClient(botToken);
		discordManager = new DiscordManager(client);
		gameScheduler = new GameScheduler(discordManager);

		try {
			id = client.getApplicationClientID();
			LOGGER.info("CanucksBot. id [" + id + "]");
		} catch (DiscordException e) {
			LOGGER.error("Failed to get Application Client ID", e);
			throw new RuntimeException(e);
		}
		EventDispatcher dispatcher = client.getDispatcher();
		dispatcher.registerListener(new ReadyListener(this));
		dispatcher.registerListener(new CommandListener(this));
	}

	static IDiscordClient getClient(String token) {
		ClientBuilder clientBuilder = new ClientBuilder();
		clientBuilder.withToken(token);

		try {
			return clientBuilder.login();
		} catch (DiscordException e) {
			LOGGER.error("Could not login.", e);
			return null;
		}
	}

	public IDiscordClient getClient() {
		return client;
	}

	public DiscordManager getDiscordManager() {
		return discordManager;
	}

	public GameScheduler getGameScheduler() {
		return gameScheduler;
	}

	public String getId() {
		return id;
	}

	/**
	 * Gets the id of the bot, in the format displayed in a message.
	 * 
	 * @return
	 */
	public String getMentionId() {
		return "<@" + id + ">";
	}
}
