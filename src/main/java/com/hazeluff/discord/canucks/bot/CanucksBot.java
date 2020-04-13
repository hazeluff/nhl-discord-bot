package com.hazeluff.discord.canucks.bot;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.canucks.Config;
import com.hazeluff.discord.canucks.bot.database.PersistentData;
import com.hazeluff.discord.canucks.bot.discord.DiscordManager;
import com.hazeluff.discord.canucks.bot.listener.MessageListener;
import com.hazeluff.discord.canucks.bot.listener.ReactionListener;
import com.hazeluff.discord.canucks.nhl.GameScheduler;
import com.hazeluff.discord.canucks.utils.Utils;
import com.mongodb.client.MongoDatabase;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.discordjson.json.gateway.StatusUpdate;
import discord4j.rest.http.client.ClientException;
import discord4j.rest.request.RouteMatcher;
import discord4j.rest.response.ResponseFunction;
import discord4j.rest.route.Routes;
import reactor.retry.Retry;

public class CanucksBot extends Thread {
	private static final Logger LOGGER = LoggerFactory.getLogger(CanucksBot.class);

	private static final StatusUpdate STARTING_UP_STATUS = Presence.doNotDisturb(Activity.watching("itself start up"));
	private static final StatusUpdate ONLINE_PRESENCE = Presence.online(Activity.playing(Config.STATUS_MESSAGE));

	private static long UPDATE_PLAY_STATUS_INTERVAL = 3600000l;

	private AtomicReference<DiscordManager> discordManager = new AtomicReference<>();
	private PersistentData preferencesManager;
	private GameScheduler gameScheduler;
	private GameDayChannelsManager gameDayChannelsManager;

	private CanucksBot() {
		preferencesManager = null;
		gameScheduler = null;
		gameDayChannelsManager = null;
	}

	CanucksBot(DiscordManager discordManager, MongoDatabase mongoDatabase,
			PersistentData preferencesManager, GameScheduler gameScheduler,
			GameDayChannelsManager gameDayChannelsManager) {
		this.discordManager.set(discordManager);
		this.preferencesManager = preferencesManager;
		this.gameScheduler = gameScheduler;
		this.gameDayChannelsManager = gameDayChannelsManager;
	}

	public static CanucksBot create(GameScheduler gameScheduler, String botToken) {
		LOGGER.info("Running CanucksBot v" + Config.VERSION);
		Thread.currentThread().setName("CanucksBot");

		CanucksBot canucksBot = new CanucksBot();
		canucksBot.gameScheduler = gameScheduler;

		// Init MongoClient/GuildPreferences
		canucksBot.initPreferences();
		// Init Discord Client
		canucksBot.initDiscord(botToken);

		while (canucksBot.getDiscordManager() == null) {
			LOGGER.info("Waiting for Discord client to be ready.");
			Utils.sleep(5000);
		}

		// Set starting up status
		canucksBot.getDiscordManager().changePresence(STARTING_UP_STATUS);

		// Attach Listeners
		attachListeners(canucksBot);

		LOGGER.info("CanucksBot Started. id [" + canucksBot.getDiscordManager().getId() + "]");

		// Start the Game Day Channels Manager
		canucksBot.initGameDayChannelsManager();

		// Manage WelcomeChannels
		LOGGER.info("Posting update to Discord channel.");
		List<Long> supportGuilds = Arrays.asList(268247727400419329l, 276953120964083713l);
		canucksBot.getDiscordManager().getClient().getGuilds()
				.filter(guild -> supportGuilds.contains(guild.getId().asLong()))
				.map(Guild::getChannels)
				.flatMap(channels -> channels.filter(channel -> 
						channel.getName().equals("welcome")).take(1))
				.filter(TextChannel.class::isInstance)
				.cast(TextChannel.class)
				.subscribe(
						channel -> WelcomeChannel.create(canucksBot, channel),
						t -> LOGGER.error("Error occurred when starting WelcomeChannel.", t));

		while (!canucksBot.getGameScheduler().isInit()) {
			LOGGER.info("Waiting for GameScheduler...");
			Utils.sleep(2000);
		}

		canucksBot.getDiscordManager().changePresence(ONLINE_PRESENCE);

		canucksBot.start();

		return canucksBot;
	}

	/**
	 * This needs to be done in its own Thread. login().block() hold the execution.
	 * 
	 * @param botToken
	 */
	private void initDiscord(String botToken) {
		new Thread(() -> {
			LOGGER.info("Initializing Discord.");
			// Init DiscordClient and DiscordManager
			DiscordClient discordClient = DiscordClientBuilder.create(botToken)
					// globally suppress any not found (404) error
					.onClientResponse(ResponseFunction.emptyIfNotFound())
					// (403) Forbidden will not be retried.
					.onClientResponse(ResponseFunction.emptyOnErrorStatus(RouteMatcher.any(), 403))
	                // (500) while creating a message will be retried, with backoff, until it succeeds
	                .onClientResponse(ResponseFunction.retryWhen(RouteMatcher.route(Routes.MESSAGE_CREATE),
	                        Retry.onlyIf(ClientException.isRetryContextStatusCode(500))
									.retryMax(3)
	                        		.fixedBackoff(Duration.ofSeconds(5))))
					.build();

			// Login
			LOGGER.info("Logging into Discord.");
			GatewayDiscordClient gatewayDiscordClient = discordClient.login().block();

			LOGGER.info("Discord Client is ready.");
			discordManager.set(new DiscordManager(gatewayDiscordClient));
		}).start();
		LOGGER.info("Discord Initializer started.");
	}

	private static void attachListeners(CanucksBot canucksBot) {
		LOGGER.info("Attaching Listeners.");
		
		Consumer<? super Throwable> logError = t -> LOGGER.error("Error occurred when responding to message.", t);
		
		MessageListener messageListener = new MessageListener(canucksBot);
		canucksBot.getDiscordManager().getClient().getEventDispatcher().on(MessageCreateEvent.class)
				.doOnError(logError)
				.subscribe(event -> messageListener.execute(event));

		ReactionListener reactionListener = new ReactionListener(canucksBot);
		canucksBot.getDiscordManager().getClient().getEventDispatcher().on(ReactionAddEvent.class)
				.doOnError(logError)
				.subscribe(event -> reactionListener.execute(event));
		canucksBot.getDiscordManager().getClient().getEventDispatcher().on(ReactionRemoveEvent.class)
				.doOnError(logError)
				.subscribe(event -> reactionListener.execute(event));
	}

	@Override
	public void run() {
		while (!isInterrupted()) {
			getDiscordManager().changePresence(ONLINE_PRESENCE);
			Utils.sleep(UPDATE_PLAY_STATUS_INTERVAL);
		}
	}

	void initPreferences() {
		LOGGER.info("Initializing Preferences.");
		this.preferencesManager = PersistentData.getInstance();
	}

	void initGameDayChannelsManager() {
		if (Config.Debug.isLoadGames()) {
			LOGGER.info("Initializing GameDayChannelsManager.");
			this.gameDayChannelsManager = new GameDayChannelsManager(this);
			gameDayChannelsManager.start();
		} else {
			LOGGER.warn("Skipping Initialization of GameDayChannelsManager");
		}
	}

	public PersistentData getPersistentData() {
		return preferencesManager;
	}

	public DiscordManager getDiscordManager() {
		return discordManager.get();
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
		return "<@" + getDiscordManager().getId().asString() + ">";
	}

	/**
	 * Gets the id of the bot, in the format displayed in a message, when the bot is
	 * mentioned by Nickname.
	 * 
	 * @return
	 */
	public String getNicknameMentionId() {
		return "<@!" + getDiscordManager().getId().asString() + ">";
	}
}
