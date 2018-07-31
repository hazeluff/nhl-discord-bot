package com.hazeluff.discord.nhlbot.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.Config;
import com.hazeluff.discord.nhlbot.bot.discord.DiscordManager;
import com.hazeluff.discord.nhlbot.bot.preferences.PreferencesManager;
import com.hazeluff.discord.nhlbot.nhl.GameScheduler;
import com.hazeluff.discord.nhlbot.utils.Utils;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.StatusType;
import sx.blah.discord.util.DiscordException;

public class NHLBot {
	private static final Logger LOGGER = LoggerFactory.getLogger(NHLBot.class);

	private IDiscordClient discordClient;
	private DiscordManager discordManager;
	private MongoDatabase mongoDatabase;
	private PreferencesManager preferencesManager;
	private GameScheduler gameScheduler;
	private GameDayChannelsManager gameDayChannelsManager;
	private String id;

	private NHLBot() {}

	NHLBot(IDiscordClient discordClient, DiscordManager discordManager, MongoDatabase mongoDatabase,
			PreferencesManager preferencesManager, GameScheduler gameScheduler,
			GameDayChannelsManager gameDayChannelsManager, String id) {
		this.discordClient = discordClient;
		this.discordManager = discordManager;
		this.mongoDatabase = mongoDatabase;
		this.preferencesManager = preferencesManager;
		this.gameScheduler = gameScheduler;
		this.gameDayChannelsManager = gameDayChannelsManager;
		this.id = id;
	}

	public static NHLBot create(String botToken, GameScheduler gameScheduler) {
		LOGGER.info("Running NHLBot v" + Config.VERSION);
		Thread.currentThread().setName("NHLBot");

		NHLBot nhlBot = new NHLBot();
		nhlBot.gameScheduler = gameScheduler;

		// Init DiscordClient and DiscordManager
		nhlBot.initDiscord(botToken);

		// Init MongoClient/GuildPreferences
		nhlBot.initPreferences();

		// Start the Game Day Channels Manager
		nhlBot.initGameDayChannelsManager();

		// Register listeners
		nhlBot.registerListeners();
		
		// Manage WelcomeChannels
		LOGGER.info("Posting update to Discord channel.");
		nhlBot.discordManager.getGuilds().stream().filter(guild -> {
			long id = guild.getLongID();
			return id == 268247727400419329l || id == 276953120964083713l;
		}).forEach(guild -> WelcomeChannel.get(nhlBot, guild));

		while (!nhlBot.getGameScheduler().isInit()) {
			LOGGER.info("Waiting for GameScheduler...");
			Utils.sleep(2000);
		}

		nhlBot.setPlayingStatus();

		return nhlBot;
	}

	void setStartingUpStatus() {
		discordManager.changePresence(StatusType.DND, ActivityType.WATCHING, "itself start up.");
	}

	void setPlayingStatus() {
		discordManager.changePresence(StatusType.ONLINE, ActivityType.PLAYING, Config.STATUS_MESSAGE);
	}

	void initDiscord(String botToken) {
		this.discordClient = getClient(botToken);
		this.discordManager = new DiscordManager(discordClient);
		this.id = discordManager.getApplicationClientId();
		LOGGER.info("NHLBot. id [" + id + "]");
	}

	static IDiscordClient getClient(String token) {
		ClientBuilder clientBuilder = new ClientBuilder();
		clientBuilder.withToken(token);
		IDiscordClient client;
		LOGGER.info("Logging in...");
		try {
			client = clientBuilder.login();
		} catch (DiscordException e) {
			LOGGER.error("Could not log in.", e);
			throw new NHLBotException(e);
		}

		while (!client.isReady()) {
			LOGGER.info("Waiting for client to be ready.");
			Utils.sleep(2000);
		}
		LOGGER.info("Client is ready.");

		return client;
	}

	void initPreferences() {
		this.mongoDatabase = getMongoDatabaseInstance();
		this.preferencesManager = PreferencesManager.getInstance(this);
	}

	void initGameDayChannelsManager() {
		this.gameDayChannelsManager = new GameDayChannelsManager(this);
		gameDayChannelsManager.start();
	}

	void registerListeners() {
		EventDispatcher dispatcher = discordClient.getDispatcher();
		dispatcher.registerListener(new MessageListener(this));
		dispatcher.registerListener(new ConnectionListener(this));
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

	/**
	 * Gets the id of the bot, in the format displayed in a message, when the bot is
	 * mentioned by Nickname.
	 * 
	 * @return
	 */
	public String getNicknameMentionId() {
		return "<@!" + id + ">";
	}
}
