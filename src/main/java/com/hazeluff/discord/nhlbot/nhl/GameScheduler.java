package com.hazeluff.discord.nhlbot.nhl;

import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.Config;
import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.utils.HttpUtils;
import com.hazeluff.discord.nhlbot.utils.Utils;

import sx.blah.discord.handle.obj.IGuild;

/**
 * This class is used to start GameTrackers for games and to maintain the channels in discord for those games.
 */
public class GameScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(GameScheduler.class);

	static final long GAME_SCHEDULE_UPDATE_RATE = 43200000L;

	// Poll for if the day has rolled over every 30 minutes
	static final long UPDATE_RATE = 1800000L;

	// I want to use TreeSet, but it removes a lot of elements for some reason...
	private Set<Game> games = new TreeSet<>(GAME_COMPARATOR);
	/**
	 * To be applied to the overall games list, so that it removes duplicates and sorts all games in order. Duplicates
	 * are determined by the gamePk being identicle. Games are sorted by game date.
	 */
	static final Comparator<Game> GAME_COMPARATOR = new Comparator<Game>() {
		public int compare(Game g1, Game g2) {
			if (g1.getGamePk() == g2.getGamePk()) {
				return 0;
			}
			int diff = g1.getDate().compareTo(g2.getDate());
			return diff == 0 ? Integer.compare(g1.getGamePk(), g2.getGamePk()) : diff;
		}
	};

	private List<GameTracker> gameTrackers = new ArrayList<>(); // TODO Change to HashMap<Integer(GamePk), GameTracker>

	private final NHLBot nhlBot;

	/**
	 * Constructor for injecting private members (Use only for testing).
	 * 
	 * @param discordmanager
	 * @param games
	 * @param gameTrackers
	 * @param teamSubscriptions
	 * @param teamLatestGames
	 */
	GameScheduler(NHLBot nhlBot, Set<Game> games, List<GameTracker> gameTrackers) {
		this.nhlBot = nhlBot;
		this.games = games;
		this.gameTrackers = gameTrackers;
	}

	public GameScheduler(NHLBot nhlBot) {
		this.nhlBot = nhlBot;
	}


	/**
	 * Starts the thread that sets up channels and polls for updates to NHLGameTrackers.
	 */
	public void run() {
		/*
		 * Initialize games, trackers, guild channels.
		 */
		initGames();
		initTrackers();
		createChannels();
		deleteInactiveChannels();

		/*
		 * Start threads to maintain the games, trackers, guild channels
		 */

		LocalDate lastUpdate = Utils.getCurrentDate(Config.DATE_START_TIME_ZONE);

		while (!isStop()) {
			LocalDate today = Utils.getCurrentDate(Config.DATE_START_TIME_ZONE);
			if (today.compareTo(lastUpdate) > 0) {
				updateGameSchedule();
				updateTrackers();
				deleteInactiveChannels();
				lastUpdate = today;
			}
			Utils.sleep(UPDATE_RATE);
		}
	}

	/**
	 * Gets game information from NHL API and initializes creates Game objects for them.
	 */
	public void initGames() {
		LOGGER.info("Initializing");
		// Retrieve schedule/game information from NHL API
		for (Team team : Team.values()) {
			ZonedDateTime startDate = ZonedDateTime.of(Config.SEASON_YEAR, 8, 1, 0, 0, 0, 0, ZoneOffset.UTC);
			ZonedDateTime endDate = ZonedDateTime.of(Config.SEASON_YEAR + 1, 6, 15, 0, 0, 0, 0, ZoneOffset.UTC);
			games.addAll(getGames(team, startDate, endDate));
		}
		LOGGER.info("Retrieved all games: [" + games.size() + "]");		

		LOGGER.info("Finished Initialization.");
	}

	/**
	 * Creates channels for all active games.
	 */
	void createChannels() {
		LOGGER.info("Creating channels for latest games.");
		for (Team team : Team.values()) {
			for(Game game : getActiveGames(team)) {
				nhlBot.getGameChannelsManager().createChannels(game, team);
			}
		}
	}

	/**
	 * Create GameTrackers for each game in the list if they are not ended.
	 * 
	 * @param list
	 *            list of games to start trackers for.
	 */
	void initTrackers() {
		LOGGER.info("Creating trackers.");
		Set<Game> activeGames = new TreeSet<>(GAME_COMPARATOR);
		for (Team team : Team.values()) {
			activeGames.addAll(getActiveGames(team));
		}
		activeGames.forEach(game -> createGameTracker(game));
	}

	/**
	 * Remove all inactive channels for all guilds subscribed. Channels are inactive if they are not in the list of
	 * latest games for the team subscribed to. Only channels written in the format that represents a game channel will
	 * be removed.
	 */
	public void deleteInactiveChannels() {
		LOGGER.info("Cleaning up old channels.");
		for (Team team : Team.values()) {
			List<Game> activeGames = getActiveGames(team);
			nhlBot.getPreferencesManager().getSubscribedGuilds(team).forEach(guild -> {
				guild.getChannels().forEach(channel -> {
					String channelName = channel.getName();
					if (Game.isFormatted(channelName)) {
						Game activeGame = activeGames.stream()
								.filter(game -> channelName.equalsIgnoreCase(game.getChannelName()))
								.findAny()
								.orElse(null);
						if (activeGame == null) {
							nhlBot.getGameChannelsManager().removeChannel(null, channel);
						} else {

						}
					}
				});
			});
		}
	}


	/**
	 * Updates the game schedule and adds games in a recent time frame to the list of games.
	 */
	void updateGameSchedule() {
		LOGGER.info("Updating game schedule.");
		// Update schedule
		for (Team team : Team.values()) {
			ZonedDateTime startDate = ZonedDateTime.now();
			ZonedDateTime endDate = startDate.plusDays(7);
			games.addAll(getGames(team, startDate, endDate));
		}
	}

	/**
	 * Removes finished trackers, and starts trackers for active games.
	 */
	void updateTrackers() {
		LOGGER.info("Removing finished trackers.");
		gameTrackers.removeIf(gameTracker -> {
			if (gameTracker.isFinished()) {
				LOGGER.info("Game is finished: " + gameTracker.getGame());
				return true;
			} else {
				return false;
			}
		});

		LOGGER.info("Starting new trackers and creating channels.");
		for (Team team : Team.values()) {
			getActiveGames(team).forEach(activeGame -> {
				createGameTracker(activeGame);
				nhlBot.getGameChannelsManager().createChannels(activeGame, team);
			});
		}
	}

	/**
	 * Initializes the channels of guild in Discord. Creates channels for the latest games of the current team the guild
	 * is subscribed to.
	 * 
	 * @param guild
	 *            guild to initialize channels for
	 */
	public void initChannels(IGuild guild) {
		LOGGER.info("Initializing channels for guild [" + guild.getName() + "]");
		Team team = nhlBot.getPreferencesManager().getTeamByGuild(guild.getLongID());

		// Create game channels of latest game for current subscribed team
		for (Game game : getActiveGames(team)) {
			nhlBot.getGameChannelsManager().createChannel(game, guild);
		}
	}

	/**
	 * Removes all channels that have names in the format of a game channel.
	 * 
	 * @param guild
	 */
	public void removeAllChannels(IGuild guild) {
		games.forEach(game -> {
			guild.getChannels().forEach(channel -> {
				if (game.getChannelName().equalsIgnoreCase(channel.getName())) {
					nhlBot.getGameChannelsManager().removeChannel(game, channel);
				}
			});
		});
	}

	/**
	 * Gets games for the specified team between the given time period.
	 * 
	 * @param team
	 *            team to get games of
	 * @return list of games
	 */
	List<Game> getGames(Team team, ZonedDateTime startDate, ZonedDateTime endDate) {
		LOGGER.info("Retrieving games of [" + team + "]");
		URIBuilder uriBuilder = null;
		String strJSONSchedule = null;
		ZonedDateTime latestDate = ZonedDateTime.of(Config.SEASON_YEAR + 1, 6, 15, 0, 0, 0, 0, ZoneOffset.UTC);
		if (endDate.compareTo(latestDate) > 0) {
			endDate = latestDate;
		}
		if (endDate.isBefore(startDate)) {
			return new ArrayList<>();
		}
		String strStartDate = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		String strEndDate = endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

		try {
			uriBuilder = new URIBuilder(Config.NHL_API_URL + "/schedule");
			uriBuilder.addParameter("startDate", strStartDate);
			uriBuilder.addParameter("endDate", strEndDate);
			uriBuilder.addParameter("teamId", String.valueOf(team.getId()));
			uriBuilder.addParameter("expand", "schedule.scoringplays");
			strJSONSchedule = HttpUtils.get(uriBuilder.build());
		} catch (URISyntaxException e) {
			String message = "Error building URI";
			RuntimeException runtimeException = new RuntimeException(message, e);
			LOGGER.error(message, runtimeException);
			throw runtimeException;
		}
		JSONObject jsonSchedule = new JSONObject(strJSONSchedule);
		JSONArray jsonDates = jsonSchedule.getJSONArray("dates");
		List<Game> games = new ArrayList<>();
		for (int i = 0; i < jsonDates.length(); i++) {
			JSONObject jsonGame = jsonDates.getJSONObject(i).getJSONArray("games").getJSONObject(0);
			Game game = new Game(jsonGame);
			if (game.getStatus() != GameStatus.SCHEDULED) {
				LOGGER.debug("Adding additional game [" + game + "]");
				games.add(game);
			}
		}
		return games;
	}

	/**
	 * Gets the latest (up to) 2 games to be used as channels in a guild. The channels can consists of the following
	 * games (priority in order).
	 * <ol>
	 * <li>Last Game</li>
	 * <li>Current Game</li>
	 * <li>Future Games</li>
	 * </ol>
	 * 
	 * @param team
	 *            team to get games of
	 * @return list of active/active games
	 */
	List<Game> getActiveGames(Team team) {
		List<Game> list = new ArrayList<>();
		Game lastGame = getLastGame(team);
		if (lastGame != null) {
			list.add(lastGame);
		}
		Game currentGame = getCurrentGame(team);
		if (currentGame != null) {
			list.add(currentGame);
		}
		int futureGameIndex = 0;
		while (list.size() < 2) {
			Game futureGame = getFutureGame(team, futureGameIndex++);
			if (futureGame != null) {
				list.add(futureGame);
			} else {
				break;
			}
		}
		return list;
	}

	/**
	 * Gets a future game for the provided team.
	 * 
	 * @param team
	 *            team to get future game for
	 * @param before
	 *            index index of how many games in the future to get (0 for first game)
	 * @return NHLGame of game in the future for the provided team
	 */
	public Game getFutureGame(Team team, int futureIndex) {
		List<Game> futureGames = games.stream()
				.filter(game -> game.containsTeam(team))
				.filter(game -> game.getStatus() == GameStatus.PREVIEW)
				.collect(Collectors.toList());
		if (futureIndex >= futureGames.size()) {
			return null;
		}
		return futureGames.get(futureIndex);
	}
	
	/**
	 * <p>
	 * Gets the next game for the provided team.
	 * </p>
	 * <p>
	 * See {@link #getFutureGame(Team, int)}
	 * </p>
	 * 
	 * @param team
	 *            team to get next game for
	 * @return NHLGame of next game for the provided team
	 */
	public Game getNextGame(Team team) {
		return getFutureGame(team, 0);
	}

	/**
	 * Gets a previous game for the provided team.
	 * 
	 * @param team
	 *            team to get previous game for
	 * @param before
	 *            index index of how many games after to get (0 for first games)
	 * @return NHLGame of next game for the provided team
	 */
	public Game getPastGame(Team team, int beforeIndex) {
		List<Game> previousGames = games.stream()
				.filter(game -> game.containsTeam(team))
				.filter(game -> game.getStatus() == GameStatus.FINAL)
				.collect(Collectors.toList());
		if (beforeIndex >= previousGames.size()) {
			return null;
		}
		return previousGames.get(previousGames.size() - 1 - beforeIndex);
	}

	/**
	 * <p>
	 * Gets the last game for the provided team.
	 * </p>
	 * <p>
	 * See {@link #getPastGame(Team, int)}
	 * </p>
	 * 
	 * @param team
	 *            team to get last game for
	 * @return NHLGame of last game for the provided team
	 */
	public Game getLastGame(Team team) {
		return getPastGame(team, 0);
	}

	/**
	 * Gets the current game for the provided team
	 * 
	 * @param team
	 *            team to get current game for
	 * @return
	 */
	public Game getCurrentGame(Team team) {
		return games.stream()
				.filter(game -> game.containsTeam(team))
				.filter(game -> game.getStatus() == GameStatus.LIVE || game.getStatus() == GameStatus.STARTED)
				.findAny()
				.orElse(null);
	}

	/**
	 * Searches all games and returns the NHLGame that would produce the same
	 * channel name as the parameter.
	 * 
	 * @param channelName
	 *            name of the Discord channel
	 * @return NHLGame that produces the same channel name<br>
	 *         null if game cannot be found; null if class is not initialized
	 * @throws NHLGameSchedulerException
	 */
	public Game getGameByChannelName(String channelName) {
		try {
			return games.stream()
					.filter(game -> game.getChannelName().equalsIgnoreCase(channelName))
					.findAny()
					.get();
		} catch (NoSuchElementException e) {
			LOGGER.warn("No channel by name [" + channelName + "]");
			return null;
		}
	}

	/**
	 * Gets the existing GameTracker for the specified game, if it exists. If the GameTracker does not exist, a new one
	 * will be instantiated and added to the tracker list.
	 * 
	 * @param game
	 *            game to find NHLGameTracker for
	 * @return NHLGameTracker for game if already exists <br>
	 *         null, if none already exists
	 * 
	 */
	GameTracker createGameTracker(Game game) {
		for (GameTracker gameTracker : gameTrackers) {
			if (gameTracker != null && gameTracker.getGame().getGamePk() == game.getGamePk()) {
				// NHLGameTracker already exists
				LOGGER.debug("NHLGameTracker exists: " + game);
				return gameTracker;
			}
		}
		LOGGER.debug("NHLGameTracker does not exist: " + game);
		GameTracker newGameTracker = new GameTracker(nhlBot.getGameChannelsManager(), game);
		if (!game.isEnded()) {
			newGameTracker.start();
			gameTrackers.add(newGameTracker);
		}
		return newGameTracker;
	}
	
	/**
	 * Gets a list games for a given team. An inactive game is one that is not in the teamLatestGames map/list.
	 * 
	 * @param team
	 *            team to get inactive games of
	 * @return list of inactive games
	 */
	List<Game> getInactiveGames(Team team) {
		return games.stream()
				.filter(game -> game.containsTeam(team))
				.filter(game -> !getActiveGames(team).contains(game))
				.collect(Collectors.toList());
	}

	/**
	 * Used for stubbing the loop of {@link #run()} for tests.
	 * 
	 * @return
	 */
	boolean isStop() {
		return false;
	}

	public Set<Game> getGames() {
		return new HashSet<>(games);
	}

	List<GameTracker> getGameTrackers() {
		return new ArrayList<>(gameTrackers);
	}
}
