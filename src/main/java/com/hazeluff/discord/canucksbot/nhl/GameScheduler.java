package com.hazeluff.discord.canucksbot.nhl;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
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

import com.hazeluff.discord.canucksbot.Config;
import com.hazeluff.discord.canucksbot.DiscordManager;
import com.hazeluff.discord.canucksbot.utils.HttpUtils;
import com.hazeluff.discord.canucksbot.utils.Utils;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;

/**
 * Class must be finished initalizing (Contructor) before other methods can be
 * used. Methods will throw {@link NHLGameSchedulerException} if not fully
 * initialized.
 * 
 * @author hazeluff
 *
 */
public class GameScheduler extends DiscordManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(GameScheduler.class);

	// Poll for if the day has rolled over every 30 minutes
	private static final int UPDATE_RATE = 1800000;

	// I want to use TreeSet, but it removes a lot of elements for some reason...
	private List<Game> games;
	private List<GameTracker> gameTrackers = new ArrayList<>();
	private Map<Team, List<IGuild>> teamSubscriptions = new HashMap<>();
	private Map<Team, List<Game>> teamLatestGames = new HashMap<>();

	public GameScheduler(IDiscordClient client) {
		super(client);
		LOGGER.info("Initializing");
		this.client = client;
		// Init variables
		for (Team team : Team.values()) {
			teamSubscriptions.put(team, new ArrayList<IGuild>());
		}
		teamLatestGames.put(Team.VANCOUVER_CANUCKS, new ArrayList<Game>());
		
		// Retrieve schedule/game information from NHL API
		Set<Game> setGames = new HashSet<>();
		for (Team team : Team.values()) {
			if(team == Team.VANCOUVER_CANUCKS) { // Skip other teams until implementing full NHL version
				LOGGER.info("Retrieving games of [" + team + "]");
				URIBuilder uriBuilder = null;
				String strJSONSchedule = "";
				try {
					uriBuilder = new URIBuilder(Config.NHL_API_URL + "/schedule");
					uriBuilder.addParameter("startDate", "2016-08-01");
					uriBuilder.addParameter("endDate", "2017-08-01");
					uriBuilder.addParameter("teamId", String.valueOf(team.getId()));
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
		}
		games = new ArrayList<>(setGames);
		Collections.sort(games, Game.getDateComparator());
		LOGGER.info("Retrieved all games: [" + games.size() + "]");
		LOGGER.info("Finished Initialization.");
	}

	/**
	 * Starts the thread that sets up channels and polls for updates to NHLGameTrackers.
	 */
	public void start() {
		class NHLGameSchedulerThread extends Thread {
			public void run() {
				LOGGER.info("Started polling thread");
				// Init NHLGameTrackers
				for (Entry<Team, List<Game>> entry : teamLatestGames.entrySet()) {
					Team team = entry.getKey();
					List<Game> latestGames = entry.getValue();
					for(Game game : Arrays.asList(getLastGame(team), getNextGame(team))) {
						latestGames.add(game);
						GameTracker newGameTracker = getGameTracker(game);
						if (!game.isEnded()) {
							newGameTracker.start();
							gameTrackers.add(newGameTracker);
						}
					}

					// Remove old channels in Discord
					for (IGuild guild : teamSubscriptions.get(team)) {
						for (IChannel channel : guild.getChannels()) {
							if (games.stream()
									.filter(game -> game.containsTeam(team) && !latestGames.contains(game))
									.anyMatch(game -> channel.getName().equalsIgnoreCase(game.getChannelName()))) {
								deleteChannel(channel);
							}
						}
					}
				}

				// Maintain them
				while (true) {
					// Remove all finished games
					removeFinishedTrackers();

					// Remove the old game in the list of latest games
					removeOldGames();

					LOGGER.info("Checking for finished games after [" + UPDATE_RATE + "]");
					Utils.sleep(UPDATE_RATE);
				}
			}
		}
		new NHLGameSchedulerThread().start();
	}

	private void removeFinishedTrackers() {
		LOGGER.info("Finding NHLGameTrackers of ended games...");
		List<GameTracker> newGameTrackers = new ArrayList<>();
		gameTrackers.removeIf(gameTracker -> {
			if (gameTracker.isFinished()) {
				// If game is finished, update latest games for each team involved in the game of the
				// finished tracker
				Game finishedGame = gameTracker.getGame();
				LOGGER.info("Game is finished: " + finishedGame);
				for (Team team : finishedGame.getTeams()) {
					if (team == Team.VANCOUVER_CANUCKS) {
						List<Game> latestGames = teamLatestGames.get(team);
						// Add the next game to the list of latest games
						Game nextGame = getNextGame(team);
						latestGames.add(nextGame);

						// Create a NHLGameTracker if one does not already exist and start it.
						GameTracker newGameTracker = getGameTracker(nextGame);
						if (!nextGame.isEnded()) {
							newGameTracker.start();
							newGameTrackers.add(newGameTracker);
						}
					}
				}

				return true;
			} else {
				return false;
			}
		});
		gameTrackers.addAll(newGameTrackers);
	}

	private void removeOldGames() {
		LOGGER.info("Finding out-of-date games to remove...");
		for (Entry<Team, List<Game>> entry : teamLatestGames.entrySet()) {
			Team team = entry.getKey();
			List<Game> latestGames = entry.getValue();
			while (latestGames.size() > 2) {
				// Remove the oldest game
				Game oldestGame = latestGames.get(0);
				LOGGER.info("Removing oldest game [" + oldestGame + "]for team [" + team + "]");
				for (IGuild guild : teamSubscriptions.get(team)) {
					for (IChannel channel : guild.getChannels()) {
						if (channel.getName().equalsIgnoreCase(oldestGame.getChannelName())) {
							deleteChannel(channel);
						}
					}
				}
				latestGames.remove(0);
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
		Date currentDate = new Date();
		List<Game> futureGames = games.stream().filter(game -> game.containsTeam(team))
				.filter(game -> game.getDate().compareTo(currentDate) >= 0).collect(Collectors.toList());
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
		Date currentDate = new Date();
		List<Game> previousGames = games.stream().filter(game -> game.containsTeam(team))
				.filter(game -> game.getDate().compareTo(currentDate) < 0).collect(Collectors.toList());
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
	 * Subscribes a channel to a game. So that events in the game are posted to
	 * the channel.
	 * 
	 * @param game
	 *            game to subscribed to
	 * @param channel
	 *            the channel to subscribe to the game
	 * @throws NHLGameSchedulerException
	 */
	public void subscribe(Team team, IGuild guild) {
		teamSubscriptions.get(team).add(guild);
	}

	/**
	 * Gets all guilds that are subscribed to the given team.
	 * 
	 * @param team
	 * @return list of guilds the subscribed to the given team
	 */
	public List<IGuild> getSubscribedGuilds(Team team) {
		return new ArrayList<>(teamSubscriptions.get(team));
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
			return games.stream().filter(game -> game.getChannelName().equalsIgnoreCase(channelName)).findAny().get();
		} catch (NoSuchElementException e) {
			LOGGER.warn("No channel by name [" + channelName + "]");
			return null;
		}
	}

	/**
	 * Gets the existing NHLGameTracker for the specified game, if it exists.
	 * 
	 * @param game
	 *            game to find NHLGameTracker for
	 * @return NHLGameTracker for game if already exists <br>
	 *         null, if none already exists
	 * 
	 */
	public GameTracker getGameTracker(Game game) {
		for (GameTracker gameTracker : gameTrackers) {
			if (gameTracker != null && gameTracker.getGame().equals(game)) {
				// NHLGameTracker already exists
				LOGGER.debug("NHLGameTracker exists: " + game.getGamePk());

				return gameTracker;
			}
		}
		LOGGER.debug("NHLGameTracker does not exist: " + game);
		return new GameTracker(client, this, game);
	}
}
