package com.hazeluff.discord.canucksbot;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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
	private String version = "?";

	public CanucksBot(String botToken) {
		client = getClient(botToken);
		nhlGameScheduler = new NHLGameScheduler(client);
		InputStream resourceAsStream = this.getClass()
				.getResourceAsStream("/META-INF/maven/com.hazeluff.discord/canucksbot/pom.properties");
		Properties prop = new Properties();
		try {
			prop.load(resourceAsStream);
			version = prop.getProperty("version");
		} catch (IOException e) {
			LOGGER.warn("Failed to get version.");
		}

		EventDispatcher dispatcher = client.getDispatcher();
		dispatcher.registerListener(new ReadyListener(this));
		dispatcher.registerListener(new CommandListener(this));
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

	public IDiscordClient getClient() {
		return client;
	}

	public NHLGameScheduler getNHLGameScheduler() {
		return nhlGameScheduler;
	}

	public String getVersion() {
		return version;
	}
}
