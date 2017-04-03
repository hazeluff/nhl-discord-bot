package com.hazeluff.discord.nhlbot.nhl;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
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
public class GameScheduler extends Thread {
	private static final Logger LOGGER = LoggerFactory.getLogger(GameScheduler.class);

	private final NHLBot nhlBot;

	// Poll for if the day has rolled over every 30 minutes
	static final int UPDATE_RATE = 1800000;

	// I want to use TreeSet, but it removes a lot of elements for some reason...
	private List<Game> games = new ArrayList<>();
	private List<GameTracker> gameTrackers = new ArrayList<>(); // TODO Change to HashMap<Integer(GamePk), GameTracker>
	private Map<Team, List<Game>> teamActiveGames = new HashMap<>();

	private boolean stop = false;

	/**
	 * Constructor for injecting private members (Use only for testing).
	 * 
	 * @param discordmanager
	 * @param games
	 * @param gameTrackers
	 * @param teamSubscriptions
	 * @param teamLatestGames
	 */
	GameScheduler(NHLBot nhlBot, List<Game> games, List<GameTracker> gameTrackers,
			Map<Team, List<Game>> teamLatestGames) {
		this.nhlBot = nhlBot;
		this.games = games;
		this.gameTrackers = gameTrackers;
		this.teamActiveGames = teamLatestGames;
	}

	public GameScheduler(NHLBot nhlBot) {
		this.nhlBot = nhlBot;
	}


	/**
	 * Starts the thread that sets up channels and polls for updates to NHLGameTrackers.
	 */
	public void run() {
		setName(GameScheduler.class.getSimpleName());

		initGames();
		// Init NHLGameTrackers
		initTeamLatestGamesLists();
		createChannels();
		createTrackers();
		deleteInactiveChannels();

		// Maintain them
		while (!isStop()) {
			// Remove all finished games
			removeFinishedTrackers();

			// Remove the inactive games from the list of latest games
			removeInactiveGames();

			LOGGER.info("Checking for finished games after [" + UPDATE_RATE + "]");
			Utils.sleep(UPDATE_RATE);
		}
	}

	/**
	 * Gets game information from NHL API and initializes creates Game objects for them.
	 */
	public void initGames() {
		LOGGER.info("Initializing");
		for (Team team : Team.values()) {
			teamActiveGames.put(team, new ArrayList<Game>());
		}

		// Retrieve schedule/game information from NHL API
		Set<Game> setGames = new HashSet<>();
		for (Team team : Team.values()) {
			LOGGER.info("Retrieving games of [" + team + "]");
			URIBuilder uriBuilder = null;
			String strJSONSchedule = "";
			try {
				uriBuilder = new URIBuilder(Config.NHL_API_URL + "/schedule");
				uriBuilder.addParameter("startDate", "2016-08-01");
				uriBuilder.addParameter("endDate", "2017-06-15");
				uriBuilder.addParameter("teamId", String.valueOf(team.getId()));
				uriBuilder.addParameter("expand", "schedule.scoringplays");
				strJSONSchedule = HttpUtils.get(uriBuilder.build());
			} catch (URISyntaxException e) {
				LOGGER.error("Error building URI", e);
			}
			JSONObject jsonSchedule = new JSONObject(strJSONSchedule);
			JSONArray jsonDates = jsonSchedule.getJSONArray("dates");
			for (int i = 0; i < jsonDates.length(); i++) {
				JSONObject jsonGame = jsonDates.getJSONObject(i).getJSONArray("games").getJSONObject(0);
				setGames.add(new Game(jsonGame));
			}
		}
		games = new ArrayList<>(setGames);
		games.sort(Game.getDateComparator());
		LOGGER.info("Retrieved all games: [" + games.size() + "]");		

		LOGGER.info("Finished Initialization.");
	}

	/**
	 * Adds the latest games for the specified team to the provided list.
	 * 
	 * @param team
	 *            team to add games for
	 * @param list
	 *            list to add games to
	 */
	void initTeamLatestGamesLists() {
		LOGGER.info("Initializing games for teams.");
		for (Entry<Team, List<Game>> entry : teamActiveGames.entrySet()) {
			Team team = entry.getKey();
			List<Game> list = entry.getValue();
			list.add(getLastGame(team));
			Game currentGame = getCurrentGame(team);
			if (currentGame != null) {
				list.add(currentGame);
			} else {
				list.add(getNextGame(team));
			}
		}
	}

	/**
	 * Creates channels for all active games.
	 */
	void createChannels() {
		LOGGER.info("Creating channels for latest games.");
		teamActiveGames.forEach(
				(team, games) -> games.forEach(game -> nhlBot.getGameChannelsManager().createChannels(game, team)));
	}

	/**
	 * Create GameTrackers for each game in the list if they are not ended.
	 * 
	 * @param list
	 *            list of games to start trackers for.
	 */
	void createTrackers() {
		LOGGER.info("Creating trackers.");
		for (List<Game> latestGames : teamActiveGames.values()) {
			for (Game game : latestGames) {
				createGameTracker(game);
			}
		}
	}

	/**
	 * Remove all inactive channels for all guilds subscribed. Channels are inactive if they are not in the list of
	 * latest games for the team subscribed to. Only channels written in the format that represents a game channel will
	 * be removed.
	 */
	public void deleteInactiveChannels() {
		LOGGER.info("Cleaning up old channels.");
		for (Team team : Team.values()) {
			List<Game> inactiveGames = getInactiveGames(team);
			nhlBot.getPreferencesManager().getSubscribedGuilds(team).forEach(guild -> {
				guild.getChannels().forEach(channel -> {
					inactiveGames.forEach(game -> {
						if (channel.getName().equalsIgnoreCase(game.getChannelName())) {
							nhlBot.getGameChannelsManager().removeChannel(game, channel);
						}
					});
				});
			});
		}
	}

	/**
	 * Removes all finished GameTrackers from the list of "live" GameTrackers. Start new trackers for the next games of
	 * the game in the finished GameTracker.
	 */
	void removeFinishedTrackers() {
		LOGGER.info("Removing GameTrackers of ended games...");
		Map<Team, Game> newGames = new HashMap<>();
		gameTrackers.removeIf(gameTracker -> {
			if (gameTracker.isFinished()) {
				// If game is finished, update latest games for each team involved in the game of the
				// finished tracker
				Game finishedGame = gameTracker.getGame();
				LOGGER.info("Game is finished: " + finishedGame);
				for (Team team : finishedGame.getTeams()) {
					List<Game> latestGames = teamActiveGames.get(team);
					// Add the next game to the list of latest games
					Game nextGame = getNextGame(team);
					latestGames.add(nextGame);
					newGames.put(team, nextGame);
				}

				return true;
			} else {
				return false;
			}
		});
		
		// Create a GameTracker for the new game
		newGames.forEach((team, newGame) -> {
			createGameTracker(newGame);
			nhlBot.getGameChannelsManager().createChannels(newGame, team);
		});
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
		Team team = nhlBot.getPreferencesManager().getTeamByGuild(guild.getID());

		// Create game channels of latest game for current subscribed team
		for (Game game : teamActiveGames.get(team)) {
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
	 * Removes the inactive games from a team's "latest games". Also removes their respective channels in Discord.
	 */
	void removeInactiveGames() {
		LOGGER.info("Removing inactive games.");
		for (Entry<Team, List<Game>> entry : teamActiveGames.entrySet()) {
			Team team = entry.getKey();
			List<Game> latestGames = entry.getValue();
			while (latestGames.size() > 2) {
				// Remove the oldest game
				Game oldestGame = latestGames.get(0);
				LOGGER.info("Removing oldest game [" + oldestGame + "] for team [" + team + "]");
				latestGames.remove(0);
				nhlBot.getGameChannelsManager().removeChannels(oldestGame, team);
			}
		}
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
			futureIndex = futureGames.size() - 1;
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
	public Game getPreviousGame(Team team, int beforeIndex) {
		List<Game> previousGames = games.stream()
				.filter(game -> game.containsTeam(team))
				.filter(game -> game.getStatus() == GameStatus.FINAL)
				.collect(Collectors.toList());
		if (beforeIndex >= previousGames.size()) {
			beforeIndex = previousGames.size() - 1;
		}
		return previousGames.get(previousGames.size() - 1 - beforeIndex);
	}

	/**
	 * <p>
	 * Gets the last game for the provided team.
	 * </p>
	 * <p>
	 * See {@link #getPreviousGame(Team, int)}
	 * </p>
	 * 
	 * @param team
	 *            team to get last game for
	 * @return NHLGame of last game for the provided team
	 */
	public Game getLastGame(Team team) {
		return getPreviousGame(team, 0);
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
			if (gameTracker != null && gameTracker.getGame().equals(game)) {
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
		.filter(game -> !teamActiveGames.get(team).contains(game))
		.collect(Collectors.toList());
	}

	/**
	 * Used for stubbing the loop of {@link #run()} for tests.
	 * 
	 * @return
	 */
	boolean isStop() {
		return stop;
	}

	public List<Game> getGames() {
		return new ArrayList<>(games);
	}

	public List<Game> getActiveGames(Team team) {
		return new ArrayList<>(teamActiveGames.get(team));
	}

	List<GameTracker> getGameTrackers() {
		return new ArrayList<>(gameTrackers);
	}
}
