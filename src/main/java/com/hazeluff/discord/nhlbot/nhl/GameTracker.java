package com.hazeluff.discord.nhlbot.nhl;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.bot.GameChannelsManager;
import com.hazeluff.discord.nhlbot.utils.DateUtils;
import com.hazeluff.discord.nhlbot.utils.Utils;

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
	private final Map<Long, String> gameReminders = new HashMap<Long, String>() {{
		put(3600000l, "60 minutes till puck drop.");
		put(1800000l, "30 minutes till puck drop.");
		put(600000l, "10 minutes till puck drop.");
	}};

	// Polling time for when game is not close to starting
	static final long IDLE_POLL_RATE_MS = 60000l;
	// Polling time for when game is started/almost-started
	static final long ACTIVE_POLL_RATE_MS = 5000l;
	// Time before game to poll faster
	static final long CLOSE_TO_START_THRESHOLD_MS = 300000l;
	// Time after game is final to continue updates
	static final long POST_GAME_UPDATE_DURATION = 600000l;

	private final GameChannelsManager gameChannelsManager;
	private final Game game;

	private boolean started = false;
	private boolean finished = false;

	public GameTracker(GameChannelsManager gameChannelsManager, Game game) {
		this.gameChannelsManager = gameChannelsManager;
		this.game = game;
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
		setName(game.getChannelName());
		LOGGER.info("Started thread for [" + game + "]");

		if (game.getStatus() != GameStatus.FINAL) {
			// Wait until close to start of game
			LOGGER.info("Idling until near game start.");
			sendReminders();

			// Game is close to starting. Poll at higher rate than previously
			LOGGER.info("Game is about to start. Polling more actively.");
			boolean alreadyStarted = waitForStart();

			// Game has started
			if (!alreadyStarted) {
				LOGGER.info("Game is about to start!");
				gameChannelsManager.sendStartOfGameMessage(game);
			} else {
				LOGGER.info("Game has already started.");
			}

			// If the game is not final after the post game updates, then it will loop back and continue to track the
			// game as if it hasn't ended yet.
			while (game.getStatus() != GameStatus.FINAL) {
				updateChannel();
				// Game is over
				gameChannelsManager.sendEndOfGameMessages(game);
				gameChannelsManager.updatePinnedMessages(game);

				// Keep checking if game is over.
				updateChannelPostGame();
			}
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
			timeTillGameMs = DateUtils.diffMs(ZonedDateTime.now(), game.getDate());
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
					gameChannelsManager.sendMessage(game, message);
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
	 * Polls at higher polling rate before game starts. Returns whether or not the game has already started
	 * 
	 * @return true, if game is already started<br>
	 *         false, otherwise
	 */
	boolean waitForStart() {
		boolean alreadyStarted = true;
		boolean started = false;
		do {
			game.update();
			started = game.getStatus() != GameStatus.PREVIEW;
			if (!started) {
				alreadyStarted = false;
				LOGGER.trace("Game almost started. Sleeping for [" + ACTIVE_POLL_RATE_MS + "]");
				Utils.sleep(ACTIVE_POLL_RATE_MS);
			}
		} while (!started);
		return alreadyStarted;
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
			gameChannelsManager.sendEventMessage(game, event);
		});
		// Update existing messages
		game.getUpdatedEvents().stream().forEach(event -> {
			gameChannelsManager.updateEventMessage(game, event);
		});
		// Delete messages of removed events
		game.getRemovedEvents().stream().forEach(event -> {
			gameChannelsManager.sendDeletedEventMessage(game, event);
		});
	}

	/**
	 * Update the Channel/Messages for a duration after the end of the game.
	 */
	void updateChannelPostGame() {
		int iterations = 0;
		while (iterations * IDLE_POLL_RATE_MS < POST_GAME_UPDATE_DURATION && game.getStatus() == GameStatus.FINAL) {
			iterations++;
			updateMessages();
			gameChannelsManager.updateEndOfGameMessages(game);
			gameChannelsManager.updatePinnedMessages(game);
			if (game.getStatus() == GameStatus.FINAL) {
				Utils.sleep(IDLE_POLL_RATE_MS);
			}
		}
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
