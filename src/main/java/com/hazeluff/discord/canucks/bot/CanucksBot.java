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
import com.hazeluff.discord.canucks.nhl.GameScheduler;
import com.hazeluff.discord.canucks.utils.Utils;
import com.mongodb.client.MongoDatabase;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.discordjson.json.gateway.StatusUpdate;
import discord4j.rest.http.client.ClientException;
import discord4j.rest.request.RouteMatcher;
import discord4j.rest.response.ResponseFunction;
import reactor.core.publisher.Mono;
import reactor.retry.Retry;
import reactor.util.function.Tuple2;

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
		LOGGER.info("Attaching Listener.");
		MessageListener messageListener = new MessageListener(canucksBot);
		canucksBot.getDiscordManager().getClient().getEventDispatcher()
				.on(MessageCreateEvent.class)
				.map(event -> messageListener.getReply(event))
				.doOnError(t -> LOGGER.error("Error occurred when responding to message.", t))
				.retry()
				.subscribe(CanucksBot::sendMessage);

		canucksBot.getDiscordManager().changePresence(STARTING_UP_STATUS);
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

	private static void sendMessage(Mono<Tuple2<Consumer<MessageCreateSpec>, TextChannel>> replyMono) {
		Tuple2<Consumer<MessageCreateSpec>, TextChannel> reply = replyMono.block();
		if (reply != null) {
			reply.getT2().createMessage(reply.getT1()).subscribe();
		}
	}

	/**
	 * This needs to be done in its own Thread. login().block() hold the execution.
	 * 
	 * @param botToken
	 */
	public void initDiscord(String botToken) {
		new Thread(() -> {
			LOGGER.info("Initializing Discord.");
			// Init DiscordClient and DiscordManager
			DiscordClient discordClient = DiscordClientBuilder.create(botToken)
					// globally suppress any not found (404) error
					.onClientResponse(ResponseFunction.emptyIfNotFound())
					// 403 Forbidden will not be retried.
					.onClientResponse(ResponseFunction
							.emptyOnErrorStatus(RouteMatcher.any(), 403))
					.onClientResponse(ResponseFunction.retryWhen(RouteMatcher.any(),
	                        Retry.onlyIf(ClientException.isRetryContextStatusCode(500))
	                                .exponentialBackoffWithJitter(
	                                		Duration.ofSeconds(2), 
	                                		Duration.ofSeconds(10))))
					.build();

			// Login
			LOGGER.info("Logging into Discord.");
			GatewayDiscordClient gatewayDiscordClient = discordClient.login().block();

			LOGGER.info("Discord Client is ready.");
			discordManager.set(new DiscordManager(gatewayDiscordClient));
		}).start();
		LOGGER.info("Discord Initializer started.");
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
