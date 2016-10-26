package com.hazeluff.discord.canucksbot.nhl;

import java.net.URISyntaxException;
import java.util.ArrayList;
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
import com.hazeluff.discord.canucksbot.utils.ThreadUtils;

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
public class NHLGameScheduler extends DiscordManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(NHLGameScheduler.class);

	// Poll for if the day has rolled over every 30 minutes
	private static final int UPDATE_RATE = 1800000;

	// I want to use TreeSet, but it removes a lot of elements for some reason...
	private List<NHLGame> games;
	private List<NHLGameTracker> gameTrackers = new ArrayList<>();
	private Map<NHLTeam, Set<IGuild>> teamSubscriptions = new HashMap<>();
	private Map<NHLTeam, List<NHLGame>> teamLatestGames = new HashMap<>();

	public NHLGameScheduler(IDiscordClient client) {
		super(client);
		LOGGER.info("Initializing");
		this.client = client;
		// Init variables
		for (NHLTeam team : NHLTeam.values()) {
			teamSubscriptions.put(team, new HashSet<IGuild>());
		}
		teamLatestGames.put(NHLTeam.VANCOUVER_CANUCKS, new ArrayList<NHLGame>());
		
		// Retrieve schedule/game information from NHL API
		Set<NHLGame> setGames = new HashSet<>();
		for (NHLTeam team : NHLTeam.values()) {
			if(team == NHLTeam.VANCOUVER_CANUCKS) { // Skip other teams until implementing full NHL version
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
					setGames.add(new NHLGame(jsonGame));
				}
				
			}
		}
		games = new ArrayList<>(setGames);
		Collections.sort(games, NHLGame.getDateComparator());
		LOGGER.info("Retrieved all games: [" + games.size() + "]");
		LOGGER.info("Finished Initialization.");
	}

	/**
	 * Starts the thread that sets up channels and polls for updates to NHLGameTrackers.
	 */
	public void start() {
		class NHLGameSchedulerThread extends Thread {

			NHLGameScheduler gameScheduler;

			public NHLGameSchedulerThread(NHLGameScheduler gameScheduler) {
				this.gameScheduler = gameScheduler;
			}

			public void run() {
				LOGGER.info("Started polling thread");
				// Init NHLGameTrackers
				for (Entry<NHLTeam, List<NHLGame>> entry : teamLatestGames.entrySet()) {
					NHLTeam team = entry.getKey();
					NHLGame lastGame = getLastGame(team);
					entry.getValue().add(lastGame);
					NHLGameTracker newGameTracker = new NHLGameTracker(client, gameScheduler, lastGame);
					newGameTracker.start();
					gameTrackers.add(newGameTracker);
					NHLGame nextGame = getNextGame(team);
					entry.getValue().add(nextGame);
					newGameTracker = new NHLGameTracker(client, gameScheduler, nextGame);
					newGameTracker.start();
					gameTrackers.add(newGameTracker);

					// Remove old channels in Discord
					for (IGuild guild : teamSubscriptions.get(team)) {
						for (IChannel channel : guild.getChannels()) {
							if (games.stream()
									.filter(game -> game.containsTeam(team) && game != lastGame && game != nextGame)
									.anyMatch(game -> channel.getName().equalsIgnoreCase(game.getChannelName()))) {
								deleteChannel(channel);
							}
						}
					}
				}

				// Maintain them
				while (true) {
					LOGGER.info("Checking for finished games.");
					gameTrackers.stream()
							// Filter for all ended game trackers
							.filter(gameTracker -> gameTracker.isEnded()).forEach(gameTracker -> {
								// Update game lists for teams involved
								for (NHLTeam team : gameTracker.getGame().getTeams()) {
									if (team == NHLTeam.VANCOUVER_CANUCKS) {
										List<NHLGame> latestGames = teamLatestGames.get(team);
										// Add the next game to the list of latest games
										NHLGame nextGame = getNextGame(team);
										latestGames.add(nextGame);

										// Create a NHLGameTracker if one does not already exist
										NHLGameTracker existingGameTracker = getExistingGameTracker(nextGame);
										if (existingGameTracker == null) {
											NHLGameTracker newGameTracker = new NHLGameTracker(client, gameScheduler,
													nextGame);
											newGameTracker.start();
											gameTrackers.add(newGameTracker);
										}

										// Remove channel of oldest game from Discord
										for (IGuild guild : teamSubscriptions.get(team)) {
											for (IChannel channel : guild.getChannels()) {
												if (channel.getName().equals(latestGames.get(0).getChannelName())) {
													deleteChannel(channel);
												}
											}
										}
										// Remove the oldest game in the list of latest games
										latestGames.remove(0);
									}
								}
							});

					LOGGER.info("Checking for finished games after [" + UPDATE_RATE + "]");
					ThreadUtils.sleep(UPDATE_RATE);
				}
			}
		}
		new NHLGameSchedulerThread(this).start();
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
	public NHLGame getFutureGame(NHLTeam team, int futureIndex) {
		Date currentDate = new Date();
		List<NHLGame> futureGames = games.stream().filter(game -> game.containsTeam(team))
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
	 * See {@link #getFutureGame(NHLTeam, int)}
	 * </p>
	 * 
	 * @param team
	 *            team to get next game for
	 * @return NHLGame of next game for the provided team
	 */
	public NHLGame getNextGame(NHLTeam team) {
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
	public NHLGame getPreviousGame(NHLTeam team, int beforeIndex) {
		Date currentDate = new Date();
		List<NHLGame> previousGames = games.stream().filter(game -> game.containsTeam(team))
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
	 * See {@link #getPreviousGame(NHLTeam, int)}
	 * </p>
	 * 
	 * @param team
	 *            team to get last game for
	 * @return NHLGame of last game for the provided team
	 */
	public NHLGame getLastGame(NHLTeam team) {
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
	public void subscribe(NHLTeam team, IGuild guild) {
		teamSubscriptions.get(team).add(guild);
	}

	/**
	 * Gets all guilds that are subscribed to the given team.
	 * 
	 * @param team
	 * @return list of guilds the subscribed to the given team
	 */
	public List<IGuild> getSubscribedGuilds(NHLTeam team) {
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
	public NHLGame getGameByChannelName(String channelName) {
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
	 */
	public NHLGameTracker getExistingGameTracker(NHLGame game) {
		for (NHLGameTracker gameTracker : gameTrackers) {
			if (gameTracker != null && gameTracker.getGame().equals(game)) {
				return gameTracker;
			}
		}
		return null;
	}
}
