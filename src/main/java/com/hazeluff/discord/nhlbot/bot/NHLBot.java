package com.hazeluff.discord.nhlbot.bot;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.Config;
import com.hazeluff.discord.nhlbot.bot.discord.DiscordManager;
import com.hazeluff.discord.nhlbot.bot.preferences.PreferencesManager;
import com.hazeluff.discord.nhlbot.nhl.GameScheduler;
import com.hazeluff.discord.nhlbot.utils.Utils;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.entity.Guild;

public class NHLBot extends Thread {
	private static final Logger LOGGER = LoggerFactory.getLogger(NHLBot.class);

	private static long UPDATE_PLAY_STATUS_INTERVAL = 3600000l;

	private DiscordClient discordClient;
	private DiscordManager discordManager;
	private MongoDatabase mongoDatabase;
	private PreferencesManager preferencesManager;
	private GameScheduler gameScheduler;
	private GameDayChannelsManager gameDayChannelsManager;
	private boolean ready = false;

	private NHLBot() {
		discordClient = null;
		discordManager = null;
		mongoDatabase = null;
		preferencesManager = null;
		gameScheduler = null;
		gameDayChannelsManager = null;
	}

	NHLBot(DiscordClient discordClient, DiscordManager discordManager, MongoDatabase mongoDatabase,
			PreferencesManager preferencesManager, GameScheduler gameScheduler,
			GameDayChannelsManager gameDayChannelsManager) {
		this.discordClient = discordClient;
		this.discordManager = discordManager;
		this.mongoDatabase = mongoDatabase;
		this.preferencesManager = preferencesManager;
		this.gameScheduler = gameScheduler;
		this.gameDayChannelsManager = gameDayChannelsManager;
	}

	public static NHLBot create(String botToken, GameScheduler gameScheduler) {
		LOGGER.info("Running NHLBot v" + Config.VERSION);
		Thread.currentThread().setName("NHLBot");

		NHLBot nhlBot = new NHLBot();
		nhlBot.gameScheduler = gameScheduler;

		// Init DiscordClient and DiscordManager
		nhlBot.discordClient = new DiscordClientBuilder(botToken).build();
		nhlBot.discordManager = new DiscordManager(nhlBot.discordClient);
		
		nhlBot.discordClient.getEventDispatcher().on(ReadyEvent.class)
				.subscribe(event -> nhlBot.ready = true);
				
		
		// TODO Uncomment
		/*
		MessageListener messageListener = new MessageListener(nhlBot);
		nhlBot.discordClient.getEventDispatcher().on(MessageCreateEvent.class)
				.map(MessageCreateEvent::getMessage)
				.subscribe(messageListener::onReceivedMessageEvent);
		*/
		
		
		// Login
		LOGGER.info("Logging into Discord.");
		nhlBot.discordClient.login().block();

		while (!nhlBot.ready) {
			LOGGER.info("Waiting for Discord client to be ready.");
			Utils.sleep(2000);
		}

		nhlBot.setStartingUpStatus();
		LOGGER.info("NHLBot. id [" + nhlBot.discordManager.getId() + "]");

		// Init MongoClient/GuildPreferences
		nhlBot.initPreferences();

		// Start the Game Day Channels Manager
		nhlBot.initGameDayChannelsManager();

		// Manage WelcomeChannels
		LOGGER.info("Posting update to Discord channel.");
		List<Long> supportGuilds = Arrays.asList(268247727400419329l, 276953120964083713l);
		nhlBot.discordClient.getGuilds()
				.filter(guild -> supportGuilds.contains(guild.getId().asLong()))
				.map(Guild::getChannels)
				.flatMap(channels -> channels.filter(channel -> channel.getName().equals("welcome")).take(1))
				.subscribe(channel -> WelcomeChannel.create(nhlBot, channel));

		while (!nhlBot.getGameScheduler().isInit()) {
			LOGGER.info("Waiting for GameScheduler...");
			Utils.sleep(2000);
		}

		nhlBot.setPlayingStatus();

		nhlBot.start();

		return nhlBot;
	}

	@Override
	public void run() {
		while (!isInterrupted()) {
			setPlayingStatus();
			Utils.sleep(UPDATE_PLAY_STATUS_INTERVAL);
		}
	}

	void setStartingUpStatus() {
		// discordManager.changePresence(StatusType.DND, ActivityType.WATCHING, "itself start up.");
	}

	void setPlayingStatus() {
		// discordManager.changePresence(StatusType.ONLINE, ActivityType.PLAYING, Config.STATUS_MESSAGE);
	}

	void initPreferences() {
		this.mongoDatabase = getMongoDatabaseInstance();
		this.preferencesManager = PreferencesManager.getInstance(this);
	}

	void initGameDayChannelsManager() {
		this.gameDayChannelsManager = new GameDayChannelsManager(this);
		gameDayChannelsManager.start();
	}

	@SuppressWarnings("resource")
	static MongoDatabase getMongoDatabaseInstance() {
		// No need to close the connection.
		return new MongoClient(Config.MONGO_HOST, Config.MONGO_PORT).getDatabase(Config.MONGO_DATABASE_NAME);
	}

	public MongoDatabase getMongoDatabase() {
		return mongoDatabase;
	}

	public PreferencesManager getPreferencesManager() {
		return preferencesManager;
	}

	public DiscordManager getDiscordManager() {
		return discordManager;
	}

	public GameScheduler getGameScheduler() {
		return gameScheduler;
	}

	public GameDayChannelsManager getGameDayChannelsManager() {
		return gameDayChannelsManager;
	}

	/**
	 * Gets the mention for the bot. It is how the raw message displays a mention of
	 * the bot's user.
	 * 
	 * @return
	 */
	public String getMention() {
		return "<@" + discordManager.getId().asString() + ">";
	}

	/**
	 * Gets the id of the bot, in the format displayed in a message, when the bot is
	 * mentioned by Nickname.
	 * 
	 * @return
	 */
	public String getNicknameMentionId() {
		return "<@!" + discordManager.getId().asString() + ">";
	}
}
