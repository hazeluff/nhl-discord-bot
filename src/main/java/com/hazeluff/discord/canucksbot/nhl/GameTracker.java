package com.hazeluff.discord.canucksbot.nhl;

import java.util.ArrayList;
import java.util.Date;
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

import sx.blah.discord.api.IDiscordClient;
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
public class GameTracker extends DiscordManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(GameTracker.class);

	// <threshold,message>
	Map<Long, String> gameReminders = new HashMap<>();

	// Poll for if game is close to starting every minute
	private static final long IDLE_POLL_RATE = 60000l;
	// Poll for game events every 5 seconds if game is close to
	// starting/started.
	private static final long ACTIVE_POLL_RATE = 5000l;
	// Time before game to poll faster
	private static final long GAME_START_THRESHOLD = 300000l;

	private boolean finished = false;
	List<IChannel> channels = new ArrayList<IChannel>();
	// Map<NHLGameEvent.idx, List<IMessage>>
	Map<Integer, List<IMessage>> eventMessages = new HashMap<Integer, List<IMessage>>();
	
	private final Game game;

	private final GameScheduler gameScheduler;

	private Thread thread;

	public GameTracker(IDiscordClient client, GameScheduler nhlGameScheduler, Game game) {
		super(client);
		this.game = game;
		this.gameScheduler = nhlGameScheduler;

		gameReminders.put(3600000l, "60 minutes till puck drop.");
		gameReminders.put(1800000l, "30 minutes till puck drop.");
		gameReminders.put(600000l, "10 minutes till puck drop.");
		gameReminders.put(300000l, "5 minutes till puck drop.");

		List<IGuild> subscribedGuilds = new ArrayList<>();
		subscribedGuilds.addAll(nhlGameScheduler.getSubscribedGuilds(game.getHomeTeam()));
		subscribedGuilds.addAll(nhlGameScheduler.getSubscribedGuilds(game.getAwayTeam()));

		String channelName = game.getChannelName();
		for (IGuild guild : subscribedGuilds) {
			if (!guild.getChannels().stream().anyMatch(c -> c.getName().equalsIgnoreCase(channelName))) {
				LOGGER.info("Creating Channel [" + channelName + "] in [" + guild.getName() + "]");
				IChannel channel = createChannel(guild, channelName);
				changeTopic(channel, "Go Canucks Go!");
				IMessage message = sendMessage(channel, game.getDetailsMessage());
				pinMessage(channel, message);
				channels.add(channel);
			} else {
				LOGGER.warn("Channel [" + channelName + "] already exists in [" + guild.getName() + "]");
				channels.add(guild.getChannels().stream()
						.filter(channel -> channel.getName().equalsIgnoreCase(channelName)).findAny().get());
			}
		}
	}

	public void start() {
		if (thread == null) {
			thread = new Thread() {
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
						sendMessage(channels, "Game is about to start. GO CANUCKS GO!");

						sendEventMessages();

						// Game is over
						sendEndOfGameMessage();
					} else {
						LOGGER.info("Game is already finished");
					}

					finished = true;
				}
			};
			thread.start();
		} else {
			LOGGER.warn("Thread already started.");
		}
	}

	void sendReminders() {
		boolean almostStart;
		do {
			long timeTillGame = Long.MAX_VALUE;
			timeTillGame = DateUtils.diff(game.getDate(), new Date());
			almostStart = timeTillGame < GAME_START_THRESHOLD;
			LOGGER.trace(timeTillGame + " " + GAME_START_THRESHOLD);
			if (!almostStart) {
				// Check to see if message should be sent.
				long lowestThreshold = Long.MAX_VALUE;
				String message = null;
				Iterator<Entry<Long, String>> it = gameReminders.entrySet().iterator();
				while (it.hasNext()) {
					Entry<Long, String> entry = it.next();
					long threshold = entry.getKey();
					if (threshold > timeTillGame) {
						if (lowestThreshold > threshold) {
							lowestThreshold = threshold;
							message = entry.getValue();
						}
						it.remove();
					}
				}
				if (message != null) {
					sendMessage(channels, message);
				}
				lowestThreshold = Long.MAX_VALUE;
				message = null;

				// Sleep (wait for
				LOGGER.trace("Idling until near game start. Sleeping for [" + IDLE_POLL_RATE + "]");
				Utils.sleep(IDLE_POLL_RATE);
			}
		} while (!almostStart);
	}

	void waitForStart() {
		boolean started = false;
		do {
			game.update();
			started = game.getStatus() != GameStatus.PREVIEW;
			if (!started) {
				LOGGER.trace("Game almost started. Sleeping for [" + ACTIVE_POLL_RATE + "]");
				Utils.sleep(ACTIVE_POLL_RATE);
			}
		} while (!started);
	}

	void sendEventMessages() {
		while (game.getStatus() != GameStatus.FINAL) {
			game.update();
			// Create new messages for new events.
			game.getNewEvents().stream().forEach(event -> {
				int eventId = event.getId();
				String message = buildEventMessage(event);
				List<IMessage> sentMessages = sendMessage(channels, message);
				eventMessages.put(eventId, sentMessages);
			});
			// Update existing messages
			game.getUpdatedEvents().stream().forEach(event -> {
				int eventId = event.getId();
				String message = buildEventMessage(event);
				if (eventMessages.containsKey(eventId)) {
					List<IMessage> sentMessages = eventMessages.get(eventId);
					updateMessage(sentMessages, message);
				}
			});
			if (game.getStatus() != GameStatus.FINAL) {
				LOGGER.trace("Game in Progress. Sleeping for [" + ACTIVE_POLL_RATE + "]");
				Utils.sleep(ACTIVE_POLL_RATE);
			}
		}
	}

	void sendEndOfGameMessage() {
		LOGGER.info("Game is over.");
		sendMessage(channels, "Game has ended. Thanks for joining!");
		sendMessage(channels, "Final Score: " + game.getScoreMessage());
		sendMessage(channels, "Goals Scored:\n" + game.getGoalsMessage());
		sendMessage(channels,
				"The next game is: " + gameScheduler.getNextGame(Team.VANCOUVER_CANUCKS).getDetailsMessage());
		for (IChannel channel : channels) {
			for (IMessage message : getPinnedMessages(channel)) {
				if (message.getAuthor().getID().equals(client.getOurUser().getID())) {
					StringBuilder strMessage = new StringBuilder();
					strMessage.append(game.getDetailsMessage()).append("\n");
					strMessage.append(game.getGoalsMessage()).append("\n");
					updateMessage(message, strMessage.toString());
				}
			}

		}
	}

	/**
	 * Build a message to deliver based on the event.
	 * 
	 * @param event
	 *            event to build message from
	 * @return message to send
	 */
	private String buildEventMessage(GameEvent event) {
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
}
