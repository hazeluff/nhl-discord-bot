package com.hazeluff.discord.canucksbot.nhl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.canucksbot.DiscordManager;
import com.hazeluff.discord.canucksbot.nhl.canucks.CanucksCustomMessages;
import com.hazeluff.discord.canucksbot.utils.DateUtils;
import com.hazeluff.discord.canucksbot.utils.Utils;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;

/**
 * <p>
 * Creates Channels in Guilds that are subscribed to the teams in this game.
 * </p>
 * 
 * <p>
 * Creates a thread that polls and sees whether a game is starting soon, and
 * triggers updates for the NHLGame.
 * </p>
 * 
 * <p>
 * Events are sent as messages to the channels created.
 * </p>
 * 
 * @author hazeluff
 *
 */
public class GameTracker extends Thread {
	private static final Logger LOGGER = LoggerFactory.getLogger(GameTracker.class);

	// <threshold,message>
	@SuppressWarnings("serial")
	private static final Map<Long, String> gameReminders = new HashMap<Long, String>() {{
		put(3600000l, "60 minutes till puck drop.");
		put(1800000l, "30 minutes till puck drop.");
		put(600000l, "10 minutes till puck drop.");
	}};

	// Poll for if game is close to starting every minute
	static final long IDLE_POLL_RATE_MS = 60000l;
	// Poll for game events every 5 seconds if game is close to
	// starting/started.
	static final long ACTIVE_POLL_RATE_MS = 5000l;
	// Time before game to poll faster
	static final long CLOSE_TO_START_THRESHOLD_MS = 300000l;
	// Time after game is final to continue updates
	static final long POST_GAME_UPDATE_DURATION = 300000l;

	private final DiscordManager discordManager;
	private final Game game;
	private final GameScheduler gameScheduler;

	private final List<IChannel> channels = new ArrayList<IChannel>();
	// Map<NHLGameEvent.idx, List<IMessage>>
	private final Map<Integer, List<IMessage>> eventMessages = new HashMap<>();
	private List<IMessage> endOfGameMessages;
	private boolean started = false;
	private boolean finished = false;

	public GameTracker(DiscordManager discordManager, GameScheduler nhlGameScheduler, Game game) {
		this.discordManager = discordManager;
		this.game = game;
		this.gameScheduler = nhlGameScheduler;

		List<IGuild> subscribedGuilds = new ArrayList<>();
		subscribedGuilds.addAll(nhlGameScheduler.getSubscribedGuilds(game.getHomeTeam()));
		subscribedGuilds.addAll(nhlGameScheduler.getSubscribedGuilds(game.getAwayTeam()));

		String channelName = game.getChannelName();
		for (IGuild guild : subscribedGuilds) {
			if (!guild.getChannels().stream().anyMatch(c -> c.getName().equalsIgnoreCase(channelName))) {
				LOGGER.info("Creating Channel [" + channelName + "] in [" + guild.getName() + "]");
				IChannel channel = discordManager.createChannel(guild, channelName);
				discordManager.changeTopic(channel, "Go Canucks Go!");
				IMessage message = discordManager.sendMessage(channel, game.getDetailsMessage());
				discordManager.pinMessage(channel, message);
				channels.add(channel);
			} else {
				LOGGER.warn("Channel [" + channelName + "] already exists in [" + guild.getName() + "]");
				channels.add(guild.getChannels().stream()
						.filter(channel -> channel.getName().equalsIgnoreCase(channelName)).findAny().get());
			}
		}
	}

	@Override
	public void start() {
		if (!started) {
			started = true;
			superStart();
		} else {
			LOGGER.warn("Thread already started.");
		}
	}

	void superStart() {
		super.start();
	}

	@Override
	public void run() {
		LOGGER.info("Started thread for [" + game + "]");
		if (game.getStatus() != GameStatus.FINAL) {
			// Wait until close to start of game
			LOGGER.info("Idling until near game start.");
			sendReminders();

			// Game is close to starting. Poll at higher rate than previously
			LOGGER.info("Game is about to start. Polling more actively.");
			waitForStart();

			// Game has started
			LOGGER.info("Game is about to start!");
			discordManager.sendMessage(channels, "Game is about to start. GO CANUCKS GO!");

			updateChannel();

			// Game is over
			sendEndOfGameMessage();
			updatePinnedMessages();

			updateChannelPostGame();
		} else {
			LOGGER.info("Game is already finished");
		}

		finished = true;
	}

	/**
	 * Sends reminders of time till the game starts.
	 */
	void sendReminders() {
		boolean firstPass = true;
		boolean closeToStart;
		do {
			long timeTillGameMs = Long.MAX_VALUE;
			timeTillGameMs = DateUtils.diffMs(LocalDateTime.now(), game.getDate());
			closeToStart = timeTillGameMs < CLOSE_TO_START_THRESHOLD_MS;
			if (!closeToStart) {
				// Check to see if message should be sent.
				long lowestThreshold = Long.MAX_VALUE;
				String message = null;
				Iterator<Entry<Long, String>> it = gameReminders.entrySet().iterator();
				while (it.hasNext()) {
					Entry<Long, String> entry = it.next();
					long threshold = entry.getKey();
					if (threshold > timeTillGameMs) {
						if (lowestThreshold > threshold) {
							lowestThreshold = threshold;
							message = entry.getValue();
						}
						it.remove();
					}
				}
				if (message != null && !firstPass) {
					discordManager.sendMessage(channels, message);
				}
				lowestThreshold = Long.MAX_VALUE;
				message = null;
				firstPass = false;
				// Sleep (wait for
				LOGGER.trace("Idling until near game start. Sleeping for [" + IDLE_POLL_RATE_MS + "]");
				Utils.sleep(IDLE_POLL_RATE_MS);
			}
		} while (!closeToStart);
	}

	/**
	 * Polls at higher polling rate before game starts.
	 */
	void waitForStart() {
		boolean started = false;
		do {
			game.update();
			started = game.getStatus() != GameStatus.PREVIEW;
			if (!started) {
				LOGGER.trace("Game almost started. Sleeping for [" + ACTIVE_POLL_RATE_MS + "]");
				Utils.sleep(ACTIVE_POLL_RATE_MS);
			}
		} while (!started);
	}

	/**
	 * Updates the Channel with Messages of events until the game is finished.
	 */
	void updateChannel() {
		while (game.getStatus() != GameStatus.FINAL) {
			updateMessages();

			if (game.getStatus() != GameStatus.FINAL) {
				LOGGER.trace("Game in Progress. Sleeping for [" + ACTIVE_POLL_RATE_MS + "]");
				Utils.sleep(ACTIVE_POLL_RATE_MS);
			}
		}
	}

	/**
	 * Updates/Posts/Removes Messages from the Channels based on the state of the Game's GameEvents.
	 */
	void updateMessages() {
		game.update();
		// Create new messages for new events.
		game.getNewEvents().stream().forEach(event -> {
			int eventId = event.getId();
			String message = buildEventMessage(event);
			List<IMessage> sentMessages = discordManager.sendMessage(channels, message);
			eventMessages.put(eventId, sentMessages);
		});
		// Update existing messages
		game.getUpdatedEvents().stream().forEach(event -> {
			int eventId = event.getId();
			String message = buildEventMessage(event);
			if (eventMessages.containsKey(eventId)) {
				List<IMessage> sentMessages = eventMessages.get(eventId);
				List<IMessage> updatedMessages = discordManager.updateMessage(sentMessages, message);
				eventMessages.put(eventId, updatedMessages);
			}
		});
		// Delete messages of removed events
		game.getRemovedEvents().stream().forEach(event -> {
			discordManager.sendMessage(channels,
					String.format("Goal by %s has been rescinded.", event.getPlayers().get(0).getFullName()));
		});
	}

	/**
	 * Send a message to channel at the end of a game to sumarize the game.
	 */
	void sendEndOfGameMessage() {
		LOGGER.debug("Sending end of game message.");
		endOfGameMessages = discordManager.sendMessage(channels, getEndOfGameMessage());
	}

	void updateEndOfGameMessage() {
		LOGGER.debug("Updating end of game message.");
		discordManager.updateMessage(endOfGameMessages, getEndOfGameMessage());
	}

	/**
	 * Update the pinned message of the channel to include details of the game.
	 */
	void updatePinnedMessages() {
		LOGGER.debug("Updating pinned messages.");
		for (IChannel channel : channels) {
			for (IMessage message : discordManager.getPinnedMessages(channel)) {
				if (discordManager.isAuthorOfMessage(message)) {
					StringBuilder strMessage = new StringBuilder();
					strMessage.append(game.getDetailsMessage()).append("\n");
					strMessage.append(game.getGoalsMessage()).append("\n");
					discordManager.updateMessage(message, strMessage.toString());
				}
			}
		}
	}

	String getEndOfGameMessage() {
		return "Game has ended. Thanks for joining!\n" +
				"Final Score: " + game.getScoreMessage() + "\n" +
				"Goals Scored:\n" + game.getGoalsMessage() + "\n" + 
				"The next game is: " + gameScheduler.getNextGame(Team.VANCOUVER_CANUCKS).getDetailsMessage();
	}

	/**
	 * Update the Channel/Messages for a duration after the end of the game.
	 */
	void updateChannelPostGame() {
		int iterations = 0;
		while (iterations * ACTIVE_POLL_RATE_MS < POST_GAME_UPDATE_DURATION) {
			iterations++;
			updateMessages();
			updateEndOfGameMessage();
			updatePinnedMessages();
			Utils.sleep(ACTIVE_POLL_RATE_MS);
		}
	}

	/**
	 * Build a message to deliver based on the event.
	 * 
	 * @param event
	 *            event to build message from
	 * @return message to send
	 */
	String buildEventMessage(GameEvent event) {
		GameEventStrength strength = event.getStrength();
		List<Player> players = event.getPlayers();
		StringBuilder message = new StringBuilder();

		// Custom goal message
		String customMessage = CanucksCustomMessages.getMessage(event.getPlayers());
		if (event.getId() % 4 == 0 && customMessage != null) {
			message.append(customMessage).append("\n");
		}

		// Regular message
		if (strength == GameEventStrength.EVEN) {
			message.append(
					String.format("%s goal by **%s**!", event.getTeam().getLocation(), players.get(0).getFullName()));
		} else {
			message.append(String.format("%s %s goal by **%s**!", event.getTeam().getLocation(),
					strength.getValue().toLowerCase(), players.get(0).getFullName()));
		}
		if (players.size() > 1) {
			message.append(String.format(" Assists: %s", players.get(1).getFullName()));
		}
		if (players.size() > 2) {
			message.append(String.format(", %s", players.get(2).getFullName()));
		}
		return message.toString();
	}

	/**
	 * Determines if this tracker is finished.
	 * 
	 * @return true, if this tracker is finished<br>
	 *         false, otherwise
	 */
	public boolean isFinished() {
		return finished;
	}

	/**
	 * Gets the game that is tracked.
	 * 
	 * @return NHLGame being tracked
	 */
	public Game getGame() {
		return game;
	}

	List<IChannel> getChannels() {
		return new ArrayList<IChannel>(channels);
	}
}
