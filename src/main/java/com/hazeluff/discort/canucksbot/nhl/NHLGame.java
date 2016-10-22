package com.hazeluff.discort.canucksbot.nhl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class NHLGame {
	private static final Logger LOGGER = LogManager.getLogger(NHLGame.class);

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	private static final SimpleDateFormat SHORT_DATE_FORMAT = new SimpleDateFormat("yy-MM-dd");
	private final Date date;
	private final int gamePk;
	private final NHLTeam awayTeam;
	private final NHLTeam homeTeam;
	private final int awayScore;
	private final int homeScore;

	public NHLGame (JSONObject jsonGame) {
		try {
			date = DATE_FORMAT.parse(jsonGame.getString("gameDate").replaceAll("Z", "+0000"));
			gamePk = jsonGame.getInt("gamePk");
			awayTeam = NHLTeam
					.parse(jsonGame.getJSONObject("teams").getJSONObject("away").getJSONObject("team").getInt("id"));
			homeTeam = NHLTeam
					.parse(jsonGame.getJSONObject("teams").getJSONObject("home").getJSONObject("team").getInt("id"));
			awayScore = jsonGame.getJSONObject("teams").getJSONObject("away").getInt("score");
			homeScore = jsonGame.getJSONObject("teams").getJSONObject("home").getInt("score");
		} catch (JSONException | ParseException e) {
			LOGGER.error(e);
			throw new RuntimeException(e);
		}
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
		return SHORT_DATE_FORMAT.format(date);
	}

	public int getGamePk() {
		return gamePk;
	}

	public NHLTeam getAwayTeam() {
		return awayTeam;
	}

	public NHLTeam getHomeTeam() {
		return homeTeam;
	}

	public int getAwayScore() {
		return awayScore;
	}

	public int getHomeScore() {
		return homeScore;
	}

	@Override
	public String toString() {
		return "NHLGame [date=" + date + ", gamePk=" + gamePk + ", awayTeam=" + awayTeam + ", homeTeam=" + homeTeam
				+ ", awayScore=" + awayScore + ", homeScore=" + homeScore + "]";
	}

	public static NHLGame nextGame(List<NHLGame> games) {
		Date currentDate = new Date();
		return games.stream().filter(d -> d.getDate().compareTo(currentDate) >= 0).findFirst().get();
	}
}
