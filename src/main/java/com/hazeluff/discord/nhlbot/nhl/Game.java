package com.hazeluff.discord.nhlbot.nhl;

import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.utils.DateUtils;
import com.hazeluff.discord.nhlbot.utils.HttpUtils;


public class Game {
	private static final Logger LOGGER = LoggerFactory.getLogger(Game.class);

	private final ZonedDateTime date;
	private final int gamePk;
	private final Team awayTeam;
	private final Team homeTeam;
	private int awayScore;
	private int homeScore;
	private GameStatus status;
	private List<GameEvent> events = new ArrayList<>();

	Game(ZonedDateTime date, int gamePk, Team awayTeam, Team homeTeam, int awayScore, int homeScore,
			GameStatus status) {
		this.date = date;
		this.gamePk = gamePk;
		this.awayTeam = awayTeam;
		this.homeTeam = homeTeam;
		this.awayScore = awayScore;
		this.homeScore = homeScore;
		this.status = status;
	}

	public Game (JSONObject jsonGame) {
		date = DateUtils.parseNHLDate(jsonGame.getString("gameDate"));
		gamePk = jsonGame.getInt("gamePk");
		awayTeam = Team
				.parse(jsonGame.getJSONObject("teams").getJSONObject("away").getJSONObject("team").getInt("id"));
		homeTeam = Team
				.parse(jsonGame.getJSONObject("teams").getJSONObject("home").getJSONObject("team").getInt("id"));
		updateInfo(jsonGame);
	}

	public ZonedDateTime getDate() {
		return date;
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

	public GameStatus getStatus() {
		return status;
	}

	public List<GameEvent> getEvents() {
		List<GameEvent> value = new ArrayList<>(events);
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
		} catch (URISyntaxException e) {
			LOGGER.error("Error building URI", e);
		}
	}

	/**
	 * Updates information about the game.
	 * <UL>
	 * <LI>Scores</LI>
	 * <LI>Status</LI>
	 * <LI>Events (Goals)</LI>
	 * </UL>
	 * 
	 * @param jsonGame
	 */
	void updateInfo(JSONObject jsonGame) {
		awayScore = jsonGame.getJSONObject("teams").getJSONObject("away").getInt("score");
		homeScore = jsonGame.getJSONObject("teams").getJSONObject("home").getInt("score");
		status = GameStatus.parse(Integer.parseInt(jsonGame.getJSONObject("status").getString("statusCode")));

		events = jsonGame.getJSONArray("scoringPlays").toList().stream()
				.map(HashMap.class::cast)
				.map(JSONObject::new)
				.map(GameEvent::new)
				.collect(Collectors.toList());
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
}
