package com.hazeluff.discord.canucksbot.nhl;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.canucksbot.utils.DateUtils;
import com.hazeluff.discord.canucksbot.utils.HttpUtils;


public class Game {
	private static final Logger LOGGER = LoggerFactory.getLogger(Game.class);

	private final Date date;
	private final int gamePk;
	private final Team awayTeam;
	private final Team homeTeam;
	private int awayScore;
	private int homeScore;
	private GameStatus status;
	private List<GameEvent> events = new ArrayList<>();
	private List<GameEvent> newEvents = new ArrayList<>();
	private List<GameEvent> updatedEvents = new ArrayList<>();

	public Game (JSONObject jsonGame) {
		date = DateUtils.parseNHLDate(jsonGame.getString("gameDate"));
		gamePk = jsonGame.getInt("gamePk");
		awayTeam = Team
				.parse(jsonGame.getJSONObject("teams").getJSONObject("away").getJSONObject("team").getInt("id"));
		homeTeam = Team
				.parse(jsonGame.getJSONObject("teams").getJSONObject("home").getJSONObject("team").getInt("id"));
		updateInfo(jsonGame);

	}

	public Date getDate() {
		return date;
	}

	/**
	 * Gets the date in the format "YY-MM-DD"
	 * 
	 * @return the date in the format "YY-MM-DD"
	 */
	public String getShortDate() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd");
		return dateFormat.format(date);
	}

	/**
	 * Gets the date in the format "EEEE dd MMM yyyy"
	 * 
	 * @return the date in the format "EEEE dd MMM yyyy"
	 */
	public String getNiceDate() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE d/MMM/yyyy");
		return dateFormat.format(date);
	}

	/**
	 * Gets the time in the format "HH:mm aaa"
	 * 
	 * @return the time in the format "HH:mm aaa"
	 */
	public String getTime() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm Z");
		return dateFormat.format(date);
	}

	String getDayOfMonthSuffix(final int n) {
		if (n >= 11 && n <= 13) {
			return "th";
		}
		switch (n % 10) {
		case 1:
			return "st";
		case 2:
			return "nd";
		case 3:
			return "rd";
		default:
			return "th";
		}
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
		String channelName = String.format("%.3s_vs_%.3s_%s",
				homeTeam.getCode(),
				awayTeam.getCode(),
				getShortDate());
		return channelName.toString();
	}

	/**
	 * Gets the message that CanucksBot will respond with when queried about
	 * this game
	 * 
	 * @return message in the format: "The next game is:\n<br>
	 *         **Home Team** vs **Away Team** at HH:mm aaa on EEEE dd MMM yyyy"
	 */
	public String getDetailsMessage() {
		String message = String.format("**%s** vs **%s** at **%s** on **%s**",
				homeTeam.getFullName(),
				awayTeam.getFullName(),
				getTime(),
				getNiceDate());
		return message.toString();
	}

	/**
	 * Gets the message that CanucksBot will respond with when queried about the
	 * score of this game
	 * 
	 * @return message in the format : "Home Team **homeScore** - **awayScore**
	 *         Away Team"
	 */
	public String getScoreMessage() {
		StringBuilder message = new StringBuilder(homeTeam.getName()).append(" **").append(homeScore)
				.append("** - **").append(awayScore).append("** ").append(awayTeam.getName());
		return message.toString();
	}

	public GameStatus getStatus() {
		return status;
	}

	public List<GameEvent> getEvents() {
		return events;
	}

	public List<GameEvent> getNewEvents() {
		List<GameEvent> value = new ArrayList<>(newEvents);
		return value;
	}

	public List<GameEvent> getUpdatedEvents() {
		List<GameEvent> value = new ArrayList<>(updatedEvents);
		return value;
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

	public static Comparator<Game> getDateComparator() {
		return new Comparator<Game>() {
			public int compare(Game g1, Game g2) {
				return (g1.date.compareTo(g2.date));
			}
		};
	}

	public boolean isOnDate(Date date) {
		return DateUtils.compareNoTime(this.date, date) == 0;
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
		} catch (URISyntaxException e) {
			LOGGER.error("Error building URI", e);
		}
		JSONObject jsonSchedule = new JSONObject(strJSONSchedule);
		JSONObject jsonGame = jsonSchedule.getJSONArray("dates").getJSONObject(0).getJSONArray("games")
				.getJSONObject(0);

		updateInfo(jsonGame);
		updateEvents(jsonGame);
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
	private void updateInfo(JSONObject jsonGame) {
		awayScore = jsonGame.getJSONObject("teams").getJSONObject("away").getInt("score");
		homeScore = jsonGame.getJSONObject("teams").getJSONObject("home").getInt("score");
		status = GameStatus.parse(Integer.parseInt(jsonGame.getJSONObject("status").getString("statusCode")));
	}

	/**
	 * Updates about events in the game
	 */
	private void updateEvents(JSONObject jsonGame) {
		newEvents.clear();
		updatedEvents.clear();
		JSONArray jsonScoringPlays = jsonGame.getJSONArray("scoringPlays");
		for (int i = 0; i < jsonScoringPlays.length(); i++) {
			GameEvent newEvent = new GameEvent(jsonScoringPlays.getJSONObject(i));
			if (!events.stream().anyMatch(event -> event.equals(newEvent))) {
				if (events.removeIf(event -> event.getId() == newEvent.getId())) {
					events.add(newEvent);
					updatedEvents.add(newEvent);
				} else {
					newEvents.add(newEvent);
				}
			}
		}

		if (!newEvents.isEmpty()) {
			newEvents.stream().forEach(event -> LOGGER.info("New event: " + event));
		}
	}

	public String getGoalsMessage() {
		List<GameEvent> goals = events;
		StringBuilder response = new StringBuilder();
		response.append("```");
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
					List<Player> players = gameEvent.getPlayers();
					response.append(String.format("\n%s - %s %-18s", 
							gameEvent.getPeriodTime(),
							gameEvent.getTeam().getCode(),
							players.get(0).getFullName()));
					if (players.size() > 1) {
						response.append("  Assists: ");
						response.append(players.get(1).getFullName());
					}
					if (players.size() > 2) {
						response.append(", ");
						response.append(players.get(2).getFullName());
					}
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
			List<Player> players = gameEvent.getPlayers();
			response.append(
					String.format("\n%s - %s %-18s", 
							gameEvent.getPeriodTime(), 
							gameEvent.getTeam().getCode(),
							players.get(0).getFullName()));
			if (players.size() > 1) {
				response.append("  Assists: ");
				response.append(players.get(1).getFullName());
			}
			if (players.size() > 2) {
				response.append(", ");
				response.append(players.get(2).getFullName());
			}
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
}
