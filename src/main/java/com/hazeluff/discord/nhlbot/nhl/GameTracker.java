package com.hazeluff.discord.nhlbot.nhl;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.bot.GameDayChannel;
import com.hazeluff.discord.nhlbot.utils.DateUtils;
import com.hazeluff.discord.nhlbot.utils.HttpException;
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

	private AtomicBoolean started = new AtomicBoolean(false);
	private AtomicBoolean finished = new AtomicBoolean(false);

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
		if (!started.get()) {
			LOGGER.info("Started thread for [" + game + "]");
			started.set(true);
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
		try {
			setName(GameDayChannel.getChannelName(game));
			if (game.getStatus() != GameStatus.FINAL) {
				// Wait until close to start of game
				LOGGER.info("Idling until near game start.");
				idleUntilNearStart();

				// Game is close to starting. Poll at higher rate than previously
				LOGGER.info("Game is about to start. Polling more actively.");
				waitForStart();

				// Game has started
				LOGGER.info("Game has started.");

				// The thread terminates when the GameStatus is Final and 10 minutes has elapsed
				ZonedDateTime lastFinal = null;
				long timeAfterLast = 0l;
				while (timeAfterLast < POST_GAME_UPDATE_DURATION) {
					updateGame();

					if (game.getStatus() == GameStatus.FINAL) {
						if (lastFinal == null) {
							LOGGER.info("Game finished. Continuing polling...");
							lastFinal = ZonedDateTime.now();
						}
						timeAfterLast = DateUtils.diffMs(ZonedDateTime.now(), lastFinal);
						LOGGER.debug("Time till thread finishes (ms): "
								+ String.valueOf(POST_GAME_UPDATE_DURATION - timeAfterLast));
					} else {
						lastFinal = null;
						LOGGER.info("Game not finished.");
					}
					Utils.sleep(ACTIVE_POLL_RATE_MS);
				}
				LOGGER.info("Game thread finished");
			} else {
				LOGGER.info("Game is already finished");
			}
		} catch (HttpException e) {
			LOGGER.error("Error occured when updating the game.", e);
		} finally {
			gameTrackers.remove(game);
			finished.set(true);
			LOGGER.info("Thread Completed");
		}
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
	 * 
	 * @throws HttpException
	 */
	void waitForStart() throws HttpException {
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
	 * 
	 * @throws HttpException
	 */
	void updateGame() throws HttpException {
		while (game.getStatus() != GameStatus.FINAL) {
			game.update();

			if (game.getStatus() != GameStatus.FINAL) {
				LOGGER.trace("Game in Progress. Sleeping for [" + ACTIVE_POLL_RATE_MS + "]");
				Utils.sleep(ACTIVE_POLL_RATE_MS);
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
		return finished.get();
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
