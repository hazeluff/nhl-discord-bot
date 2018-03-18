package com.hazeluff.discord.nhlbot.nhl;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.bot.GameDayChannel;
import com.hazeluff.discord.nhlbot.utils.DateUtils;
import com.hazeluff.discord.nhlbot.utils.Utils;

/**
 * <p>
 * Creates Channels in Guilds that are subscribed to the teams in this game.
 * </p>
 * 
 * <p>
 * Creates a thread that updates a {@link NHLGame}
 * </p>
 * 
 * <p>
 * Events are sent as messages to the channels created.
 * </p>
 */
public class GameTracker extends Thread {
	private static final Logger LOGGER = LoggerFactory.getLogger(GameTracker.class);

	// Polling time for when game is not close to starting
	static final long IDLE_POLL_RATE_MS = 60000l;
	// Polling time for when game is started/almost-started
	static final long ACTIVE_POLL_RATE_MS = 5000l;
	// Time before game to poll faster
	static final long CLOSE_TO_START_THRESHOLD_MS = 300000l;
	// Time after game is final to continue updates
	static final long POST_GAME_UPDATE_DURATION = 600000l;

	private static Map<Game, GameTracker> gameTrackers = new ConcurrentHashMap<>();

	private final Game game;

	private boolean started = false;
	private boolean finished = false;

	GameTracker(Game game) {
		this.game = game;
	}

	/**
	 * Gets an instance of a {@link GameTracker} for the given game. The tracker
	 * thread is started on instantiation.
	 * 
	 * @param game
	 *            game to get {@link GameTracker} for
	 * @return {@link GameTracker} for the game
	 */
	public static GameTracker get(Game game) {
		GameTracker gameTracker = new GameTracker(game);
		gameTracker.start();
		return gameTracker;
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
		setName(GameDayChannel.getChannelName(game));
		LOGGER.info("Started thread for [" + game + "]");

		if (game.getStatus() != GameStatus.FINAL) {
			// Wait until close to start of game
			LOGGER.info("Idling until near game start.");
			idleUntilNearStart();

			// Game is close to starting. Poll at higher rate than previously
			LOGGER.info("Game is about to start. Polling more actively.");
			waitForStart();

			// Game has started
			LOGGER.info("Game has started.");

			// If the game is not final after the post game updates, then it will loop back and continue to track the
			// game as if it hasn't ended yet.
			while (game.getStatus() != GameStatus.FINAL) {
				updateGame();

				// Keep checking if game is actually over.
				updatePostGame();
			}
		} else {
			LOGGER.info("Game is already finished");
		}

		gameTrackers.remove(game);
		finished = true;
	}

	/**
	 * Idles until we are close to the start of the game.
	 */
	void idleUntilNearStart() {
		boolean closeToStart;
		long timeTillGameMs = Long.MAX_VALUE;
		do {
			timeTillGameMs = DateUtils.diffMs(ZonedDateTime.now(), game.getDate());
			closeToStart = timeTillGameMs < CLOSE_TO_START_THRESHOLD_MS;
			if (!closeToStart) {
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
	 * Updates the game.
	 */
	void updateGame() {
		while (game.getStatus() != GameStatus.FINAL) {
			game.update();

			if (game.getStatus() != GameStatus.FINAL) {
				LOGGER.trace("Game in Progress. Sleeping for [" + ACTIVE_POLL_RATE_MS + "]");
				Utils.sleep(ACTIVE_POLL_RATE_MS);
			}
		}
	}

	/**
	 * Update the game for a duration after the end of the game.
	 */
	void updatePostGame() {
		int iterations = 0;
		while (iterations * IDLE_POLL_RATE_MS < POST_GAME_UPDATE_DURATION && game.getStatus() == GameStatus.FINAL) {
			iterations++;
			game.update();
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
