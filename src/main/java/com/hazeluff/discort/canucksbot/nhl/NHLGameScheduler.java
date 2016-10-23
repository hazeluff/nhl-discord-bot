package com.hazeluff.discort.canucksbot.nhl;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.http.client.utils.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.hazeluff.discort.canucksbot.utils.DateUtils;
import com.hazeluff.discort.canucksbot.utils.HttpUtils;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;

/**
 * Class must be finished initalizing (Contructor) before other methods can be
 * used. Methods will throw {@link NHLGameSchedulerException} if not fully
 * initialized.
 * 
 * @author hazeluff
 *
 */
public class NHLGameScheduler extends Thread {
	private static final Logger LOGGER = LogManager.getLogger(NHLGameScheduler.class);

	// Poll for if the day has rolled over every 30 minutes
	private static final int POLL_RATE = 1800000;

	private static boolean ready = false;

	// I want to use TreeSet, but it removes a lot of elements for some reason...
	private static List<NHLGame> games;
	private static Map<NHLTeam, Set<IGuild>> teamSubscriptions = new HashMap<>();
	private static List<NHLGameTracker> todayGames;

	private IDiscordClient client;

	public NHLGameScheduler(IDiscordClient client) {
		LOGGER.info("Initializing");
		// Init variables
		this.client = client;
		for (NHLTeam team : NHLTeam.values()) {
			teamSubscriptions.put(team, new HashSet<IGuild>());
		}
		
		// Retrieve schedule/game information from NHL API
		Set<NHLGame> setGames = new HashSet<>();
		for (NHLTeam team : NHLTeam.values()) {
			if(team == NHLTeam.VANCOUVER_CANUCKS) { // Skip other teams until implementing full NHL version
				LOGGER.info("Retrieving games of [" + team + "]");
				URIBuilder uriBuilder = null;
				String strJSONSchedule = "";
				try {
					uriBuilder = new URIBuilder("https://statsapi.web.nhl.com/api/v1/schedule");
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

		ready = true;
		LOGGER.info("Finished Initialization.");
	}

	public static NHLGame nextGame(NHLTeam team) {
		if (ready) {
		Date currentDate = new Date();
		return games.stream().filter(game -> game.containsTeam(team))
				.filter(game -> game.getDate().compareTo(currentDate) >= 0)
				.findFirst()
				.get();
		}
		throw new NHLGameSchedulerException("Not yet initialized");
	}


	/**
	 * Poll to see if the date has rolled over.
	 */
	public void run() {
		if (ready) {
			Date lastDate = null;

			while (true) {
				LOGGER.info("Started.");
				Date currentDate = new Date();
				LOGGER.debug("Last Date: " + lastDate + ", Current Date: " + currentDate);
				todayGames = new ArrayList<>();
				if (lastDate == null || DateUtils.compareNoTime(lastDate, currentDate) < 0) {
					lastDate = currentDate;
					games.stream().filter(game -> game.isOnDate(currentDate)).forEach(game -> {
						todayGames.add(new NHLGameTracker(client, game));
					});
				}
				try {
					LOGGER.info("Sleeping for [" + POLL_RATE + "]");
					sleep(POLL_RATE);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} else {
			throw new NHLGameSchedulerException("Not yet initialized");
		}
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
	public static void subscribe(NHLTeam team, IGuild guild) {
		if (ready) {
			teamSubscriptions.get(team).add(guild);
		} else {
			throw new NHLGameSchedulerException("Not yet initialized");
		}
	}

	public static List<IGuild> getSubscribedGuilds(NHLTeam team) {
		if (ready) {
			return new ArrayList<>(teamSubscriptions.get(team));
		}
		throw new NHLGameSchedulerException("Not yet initialized");
	}

	/**
	 * Determines if this object has finished initializing.
	 * 
	 * @return true, if finished; <br>
	 *         false, otherwise
	 */
	public boolean isReady() {
		return ready;
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
	public static NHLGame getGameByChannelName(String channelName) {
		if (ready) {
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
		throw new NHLGameSchedulerException("Not yet initialized");
	}
}
