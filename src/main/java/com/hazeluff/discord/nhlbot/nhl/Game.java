package com.hazeluff.discord.nhlbot.nhl;

import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.client.utils.URIBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.utils.DateUtils;
import com.hazeluff.discord.nhlbot.utils.HttpException;
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

	private Game(ZonedDateTime date, int gamePk, Team awayTeam, Team homeTeam) {
		this.date = date;
		this.gamePk = gamePk;
		this.awayTeam = awayTeam;
		this.homeTeam = homeTeam;
	}

	public static Game parse(JSONObject jsonGame) {
		try {
			ZonedDateTime date = DateUtils.parseNHLDate(jsonGame.getString("gameDate"));
			int gamePk = jsonGame.getInt("gamePk");
			Team awayTeam = Team
					.parse(jsonGame.getJSONObject("teams").getJSONObject("away").getJSONObject("team").getInt("id"));
			Team homeTeam = Team
					.parse(jsonGame.getJSONObject("teams").getJSONObject("home").getJSONObject("team").getInt("id"));
			Game game = new Game(date, gamePk, awayTeam, homeTeam);
			game.updateState(jsonGame);

			return game;
		} catch (Exception e) {
			LOGGER.error("Could not parse game.", e);
			return null;
		}
	}

	/**
	 * Clones the values (that are dynamic) in the provided game, and applies it to
	 * this game.
	 * 
	 * @param updatedGame
	 */
	public void updateTo(Game updatedGame) {
		awayScore = updatedGame.getAwayScore();
		homeScore = updatedGame.getHomeScore();
		status = updatedGame.getStatus();
		events = updatedGame.getEvents();
	}

	/**
	 * Calls the NHL API and gets the current information of the game.
	 * 
	 * @throws HttpException
	 */
	public void update() throws HttpException {
		LOGGER.trace("Updating. [" + gamePk + "]");
		String strJSONSchedule = "";
		try {
			URIBuilder uriBuilder = new URIBuilder("https://statsapi.web.nhl.com/api/v1/schedule");
			uriBuilder.addParameter("gamePk", Integer.toString(gamePk));
			uriBuilder.addParameter("expand", "schedule.scoringplays");
			strJSONSchedule = HttpUtils.getAndRetry(uriBuilder.build(), 5, // 5 retries
					60000l, //
					"Update the game.");
		} catch (URISyntaxException e) {
			LOGGER.error("Error building URI", e);
		}

		if (strJSONSchedule.isEmpty()) {
			return;
		}

		try {
			JSONObject jsonSchedule = new JSONObject(strJSONSchedule);
			JSONObject jsonGame = jsonSchedule.getJSONArray("dates").getJSONObject(0).getJSONArray("games")
					.getJSONObject(0);
			updateState(jsonGame);
		} catch (JSONException e) {
			LOGGER.error("Failed to parse game.", e);
		}
	}

	/**
	 * Updates the state of the game.
	 * <UL>
	 * <LI>Scores</LI>
	 * <LI>Status</LI>
	 * <LI>Events (Goals)</LI>
	 * </UL>
	 * 
	 * @param jsonGame
	 */
	void updateState(JSONObject jsonGame) {
		awayScore = jsonGame.getJSONObject("teams").getJSONObject("away").getInt("score");
		homeScore = jsonGame.getJSONObject("teams").getJSONObject("home").getInt("score");
		status = GameStatus.parse(Integer.parseInt(jsonGame.getJSONObject("status").getString("statusCode")));

		events = jsonGame.getJSONArray("scoringPlays").toList().stream().map(HashMap.class::cast).map(JSONObject::new)
				.map(GameEvent::parse).collect(Collectors.toList());
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
		return new ArrayList<>(events);
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
	 * Determines if game is ended.
	 * 
	 * @return true, if game has ended<br>
	 *         false, otherwise
	 */
	public boolean isEnded() {
		return status == GameStatus.FINAL;
	}
}
