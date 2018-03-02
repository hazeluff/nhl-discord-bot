package com.hazeluff.discord.nhlbot.nhl;

import java.net.URISyntaxException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.utils.DateUtils;
import com.hazeluff.discord.nhlbot.utils.HttpUtils;


public class Game {
	private static final Logger LOGGER = LoggerFactory.getLogger(Game.class);

	// Number of retries to do when NHL API returns no events.
	static final int NHL_EVENTS_RETRIES = 5;

	private final ZonedDateTime date;
	private final int gamePk;
	private final Team awayTeam;
	private final Team homeTeam;
	private int awayScore;
	private int homeScore;
	private GameStatus status;
	private final List<GameEvent> events = new ArrayList<>();
	private final List<GameEvent> newEvents = new ArrayList<>();
	private final List<GameEvent> updatedEvents = new ArrayList<>();
	private final List<GameEvent> removedEvents = new ArrayList<>();
	private int eventsRetries = 0;
	

	Game(ZonedDateTime date, int gamePk, Team awayTeam, Team homeTeam, int awayScore, int homeScore,
			GameStatus status, List<GameEvent> events, List<GameEvent> newEvents, List<GameEvent> updatedEvents,
			List<GameEvent> removedEvents) {
		this.date = date;
		this.gamePk = gamePk;
		this.awayTeam = awayTeam;
		this.homeTeam = homeTeam;
		this.awayScore = awayScore;
		this.homeScore = homeScore;
		this.status = status;
		this.events.addAll(events);
		this.newEvents.addAll(newEvents);
		this.updatedEvents.addAll(updatedEvents);
		this.removedEvents.addAll(removedEvents);
	}

	public Game (JSONObject jsonGame) {
		date = DateUtils.parseNHLDate(jsonGame.getString("gameDate"));
		gamePk = jsonGame.getInt("gamePk");
		awayTeam = Team
				.parse(jsonGame.getJSONObject("teams").getJSONObject("away").getJSONObject("team").getInt("id"));
		homeTeam = Team
				.parse(jsonGame.getJSONObject("teams").getJSONObject("home").getJSONObject("team").getInt("id"));
		updateInfo(jsonGame);
		updateEvents(jsonGame);
		newEvents.clear();
		updatedEvents.clear();
	}

	public ZonedDateTime getDate() {
		return date;
	}

	/**
	 * Gets the date in the format "YY-MM-DD"
	 * 
	 * @param zone
	 *            time zone to convert the time to
	 * @return the date in the format "YY-MM-DD"
	 */
	public String getShortDate(ZoneId zone) {
		return date.withZoneSameInstant(zone)
				.format(DateTimeFormatter.ofPattern("yy-MM-dd"));
	}

	/**
	 * Gets the date in the format "EEEE dd MMM yyyy"
	 * 
	 * @param zone
	 *            time zone to convert the time to
	 * @return the date in the format "EEEE dd MMM yyyy"
	 */
	public String getNiceDate(ZoneId zone) {
		return date.withZoneSameInstant(zone)
				.format(DateTimeFormatter.ofPattern("EEEE d/MMM/yyyy"));
	}

	/**
	 * Gets the time in the format "HH:mm aaa"
	 * 
	 * @param zone
	 *            time zone to convert the time to
	 * @return the time in the format "HH:mm aaa"
	 */
	public String getTime(ZoneId zone) {
		return date.withZoneSameInstant(zone)
				.format(DateTimeFormatter.ofPattern("H:mm z"));
	}

	public int getGamePk() {
		return gamePk;
	}

	public Team getAwayTeam() {
		return awayTeam;
	}

	public Team getHomeTeam() {
		return homeTeam;
	}

	/**
	 * Gets both home and aways teams as a list
	 * 
	 * @return list containing both home and away teams
	 */
	public List<Team> getTeams() {
		return Arrays.asList(homeTeam, awayTeam);
	}

	/**
	 * Determines if the given team is participating in this game
	 * 
	 * @param team
	 * @return true, if team is a participant<br>
	 *         false, otherwise
	 */
	public boolean isContain(Team team) {
		return homeTeam == team || awayTeam == team;
	}

	public boolean containsTeam(Team team) {
		return awayTeam == team || homeTeam == team;
	}

	public int getAwayScore() {
		return awayScore;
	}

	public int getHomeScore() {
		return homeScore;
	}

	/**
	 * Gets the name that a channel in Discord related to this game would have.
	 * 
	 * @return channel name in format: "AAA_vs_BBB-yy-MM-DD". <br>
	 *         AAA is the 3 letter code of home team<br>
	 *         BBB is the 3 letter code of away team<br>
	 *         yy-MM-DD is a date format
	 */
	public String getChannelName() {
		String channelName = String.format("%.3s-vs-%.3s-%s",
				homeTeam.getCode(),
				awayTeam.getCode(),
				getShortDate(ZoneId.of("America/New_York")));
		return channelName.toLowerCase();
	}

	/**
	 * Gets the message that NHLBot will respond with when queried about this game
	 * 
	 * @param timeZone
	 *            the time zone to localize to
	 * 
	 * @return message in the format: "The next game is:\n<br>
	 *         **Home Team** vs **Away Team** at HH:mm aaa on EEEE dd MMM yyyy"
	 */
	public String getDetailsMessage(ZoneId timeZone) {
		String message = String.format("**%s** vs **%s** at **%s** on **%s**",
				homeTeam.getFullName(),
				awayTeam.getFullName(),
				getTime(timeZone),
				getNiceDate(timeZone));
		return message.toString();
	}

	/**
	 * Gets the message that NHLBot will respond with when queried about the
	 * score of this game
	 * 
	 * @return message in the format : "Home Team **homeScore** - **awayScore**
	 *         Away Team"
	 */
	public String getScoreMessage() {
		return String.format("%s **%s** - **%s** %s", homeTeam.getName(), homeScore, awayScore,
				awayTeam.getName());
	}

	public GameStatus getStatus() {
		return status;
	}

	public List<GameEvent> getEvents() {
		List<GameEvent> value = new ArrayList<>(events);
		return value;
	}

	public List<GameEvent> getNewEvents() {
		return new ArrayList<>(newEvents);
	}

	public List<GameEvent> getUpdatedEvents() {
		return new ArrayList<>(updatedEvents);
	}

	List<GameEvent> getRemovedEvents() {
		return new ArrayList<>(removedEvents);
	}

	@Override
	public String toString() {
		return "NHLGame [date=" + date + ", gamePk=" + gamePk + ", awayTeam=" + awayTeam + ", homeTeam=" + homeTeam
				+ ", awayScore=" + awayScore + ", homeScore=" + homeScore + ", status=" + status + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + awayScore;
		result = prime * result + ((awayTeam == null) ? 0 : awayTeam.hashCode());
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + gamePk;
		result = prime * result + homeScore;
		result = prime * result + ((homeTeam == null) ? 0 : homeTeam.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Game other = (Game) obj;
		if (awayScore != other.awayScore)
			return false;
		if (awayTeam != other.awayTeam)
			return false;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (gamePk != other.gamePk)
			return false;
		if (homeScore != other.homeScore)
			return false;
		if (homeTeam != other.homeTeam)
			return false;
		if (status != other.status)
			return false;
		return true;
	}

	public boolean equals(Game other) {
		return gamePk == other.gamePk;
	}

	/**
	 * Calls the NHL API and gets the current information of the game and
	 * updates all the update-able members in this class
	 */
	public void update() {
		LOGGER.trace("Updating. [" + gamePk + "]");
		URIBuilder uriBuilder = null;
		String strJSONSchedule = "";
		try {
			uriBuilder = new URIBuilder("https://statsapi.web.nhl.com/api/v1/schedule");
			uriBuilder.addParameter("gamePk", Integer.toString(gamePk));
			uriBuilder.addParameter("expand", "schedule.scoringplays");
			strJSONSchedule = HttpUtils.get(uriBuilder.build());
			JSONObject jsonSchedule = new JSONObject(strJSONSchedule);
			JSONObject jsonGame = jsonSchedule.getJSONArray("dates").getJSONObject(0).getJSONArray("games")
					.getJSONObject(0);
			updateInfo(jsonGame);
			updateEvents(jsonGame);
		} catch (URISyntaxException e) {
			LOGGER.error("Error building URI", e);
		}
	}

	/**
	 * Updates information about the game.
	 * <UL>
	 * <LI>Scores</LI>
	 * <LI>Status</LI>
	 * </UL>
	 * 
	 * @param jsonGame
	 */
	void updateInfo(JSONObject jsonGame) {
		awayScore = jsonGame.getJSONObject("teams").getJSONObject("away").getInt("score");
		homeScore = jsonGame.getJSONObject("teams").getJSONObject("home").getInt("score");
		status = GameStatus.parse(Integer.parseInt(jsonGame.getJSONObject("status").getString("statusCode")));
	}

	/**
	 * Updates about events in the game
	 */
	void updateEvents(JSONObject jsonGame) {
		newEvents.clear();
		updatedEvents.clear();
		removedEvents.clear();
		JSONArray jsonScoringPlays = jsonGame.getJSONArray("scoringPlays");
		List<GameEvent> retrievedEvents = new ArrayList<>();
		for (int i = 0; i < jsonScoringPlays.length(); i++) {
			retrievedEvents.add(new GameEvent(jsonScoringPlays.getJSONObject(i)));
		}

		if (retrievedEvents.isEmpty()) {
			if (events.size() > 1) {
				LOGGER.warn("NHL api returned no events, but we have stored more than one event.");
				return;
			} else if (events.size() == 1) {
				LOGGER.warn("NHL api returned no events, but we have stored one event.");
				if (eventsRetries++ < NHL_EVENTS_RETRIES) {
					LOGGER.warn(String.format("Could be a rescinded goal or NHL api issue. "
							+ "Retrying %s time(s) out of %s", eventsRetries, NHL_EVENTS_RETRIES));
					return;
				}
			}
		}
		eventsRetries = 0;

		retrievedEvents.forEach(retrievedEvent -> {
			if (!retrievedEvent.getPlayers().isEmpty()
					&& !events.stream().anyMatch(event -> event.equals(retrievedEvent))) {
				if (events.removeIf(event -> event.getId() == retrievedEvent.getId())) {
					// Updated events
					LOGGER.debug("Updated event: [" + retrievedEvent + "]");
					updatedEvents.add(retrievedEvent);
				} else {
					// New events
					LOGGER.debug("New event: [" + retrievedEvent + "]");
					newEvents.add(retrievedEvent);
				}
				events.add(retrievedEvent);
			}
		});

		// Deleted events
		events.removeIf(event -> {
			if (!retrievedEvents.contains(event)) {
				LOGGER.debug("Removed event: [" + event + "]");
				removedEvents.add(event);
				return true;
			}
			return false;
		});
	}

	public String getGoalsMessage() {
		List<GameEvent> goals = events;
		StringBuilder response = new StringBuilder();
		response.append("```\n");
		for (int i = 1; i <= 3; i++) {
			switch (i) {
			case 1:
				response.append("1st Period:");
				break;
			case 2:
				response.append("\n\n2nd Period:");
				break;
			case 3:
				response.append("\n\n3rd Period:");
				break;
			}
			int period = i;
			Predicate<GameEvent> isPeriod = gameEvent -> gameEvent.getPeriod().getPeriodNum() == period;
			if (goals.stream().anyMatch(isPeriod)) {
				for (GameEvent gameEvent : goals.stream().filter(isPeriod)
						.collect(Collectors.toList())) {
					response.append("\n").append(gameEvent.getDetails());
				}
			} else {
				response.append("\nNone");
			}
		}
		Predicate<GameEvent> isOtherPeriod = gameEvent -> gameEvent.getPeriod().getPeriodNum() > 3;
		if (goals.stream().anyMatch(isOtherPeriod)) {
			GameEvent gameEvent = goals.stream().filter(isOtherPeriod).findFirst().get();
			GamePeriod period = gameEvent.getPeriod();
			response.append("\n\n").append(period.getDisplayValue()).append(":");
			goals.stream().filter(isOtherPeriod).forEach(event -> response.append("\n").append(event.getDetails()));
		}
		response.append("\n```");
		return response.toString();
	}

	/**
	 * Determines if game is ended.
	 * 
	 * @return true, if game has ended<br>
	 *         false, otherwise
	 */
	public boolean isEnded() {
		return status == GameStatus.FINAL;
	}

	/**
	 * Determines if the given channel name is that of a possible game. Does not factor into account whether or not the
	 * game is real.
	 * 
	 * @param channelName
	 *            name of the channel
	 * @return true, if is of game channel format;<br>
	 *         false, otherwise.
	 */
	public static boolean isFormatted(String channelName) {
		String teamRegex = String.join("|", Arrays.asList(Team.values()).stream()
				.map(team -> team.getCode().toLowerCase()).collect(Collectors.toList()));
		teamRegex = String.format("(%s)", teamRegex);
		String regex = String.format("%1$s_vs_%1$s_[0-9]{2}-[0-9]{2}-[0-9]{2}", teamRegex);
		return channelName.matches(regex);
	}
}
