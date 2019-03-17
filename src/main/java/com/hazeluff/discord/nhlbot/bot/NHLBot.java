package com.hazeluff.discord.nhlbot.bot;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

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
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.core.spec.MessageCreateSpec;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;

public class NHLBot extends Thread {
	private static final Logger LOGGER = LoggerFactory.getLogger(NHLBot.class);

	private static final Presence STARTING_UP_PRESENCE = Presence.doNotDisturb(Activity.watching("itself start up"));
	private static final Presence ONLINE_PRESENCE = Presence.online(Activity.playing(Config.STATUS_MESSAGE));

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
				
		MessageListener messageListener = new MessageListener(nhlBot);
		nhlBot.discordClient.getEventDispatcher().on(MessageCreateEvent.class)
				.flatMap(NHLBot::zipEvent)
				.subscribe(t -> {
					Consumer<MessageCreateSpec> replySpec = messageListener.getReply(t.getT1(), t.getT2(), t.getT3());
					if (replySpec != null) {
						t.getT2().createMessage(replySpec);
					}
				});

		// Login
		LOGGER.info("Logging into Discord.");
		nhlBot.discordClient.login().block();

		while (!nhlBot.ready) {
			LOGGER.info("Waiting for Discord client to be ready.");
			Utils.sleep(2000);
		}

		nhlBot.discordManager.changePresence(STARTING_UP_PRESENCE);
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
				.flatMap(channels -> channels.filter(channel -> 
						channel.getName().equals("welcome")).take(1))
				.filter(TextChannel.class::isInstance)
				.cast(TextChannel.class)
				.subscribe(channel -> WelcomeChannel.create(nhlBot, channel));

		while (!nhlBot.getGameScheduler().isInit()) {
			LOGGER.info("Waiting for GameScheduler...");
			Utils.sleep(2000);
		}

		nhlBot.discordManager.changePresence(ONLINE_PRESENCE);

		nhlBot.start();

		return nhlBot;
	}

	/**
	 * Takes a event and returns a Mono that zips up the following properties of the
	 * event (in order):
	 * <ol>
	 * <li>guild</li>
	 * <li>channel</li>
	 * <li>message</li>
	 * </ol>
	 * 
	 * @param event
	 *            the event to zip
	 * @return {@link Tuple3} containing (in order): guild, channel, message
	 */
	private static Mono<Tuple3<Guild, MessageChannel, Message>> zipEvent(MessageCreateEvent event) {
		return Mono.zip(event.getGuild(), event.getMessage().getChannel(), Mono.just(event.getMessage()));
	}

	@Override
	public void run() {
		while (!isInterrupted()) {
			discordManager.changePresence(ONLINE_PRESENCE);
			Utils.sleep(UPDATE_PLAY_STATUS_INTERVAL);
		}
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
