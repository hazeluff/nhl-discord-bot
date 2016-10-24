package com.hazeluff.discord.canucksbot.nhl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.hazeluff.discord.canucksbot.utils.DateUtils;

public class NHLGameEvent {
	private final int id;
	private int idx;
	private Date date;
	private final NHLGameEventType type;
	private final NHLTeam team;
	private final String periodTime;
	private final NHLGamePeriod period;
	private final List<NHLPlayer> players = new ArrayList<>();
	private final NHLGameEventStrength strength;

	public NHLGameEvent(JSONObject jsonScoringPlays) {
		JSONObject jsonAbout = jsonScoringPlays.getJSONObject("about");
		id = jsonAbout.getInt("eventId");
		idx = jsonAbout.getInt("eventIdx");
		date = DateUtils.parseNHLDate(jsonAbout.getString("dateTime"));
		period = new NHLGamePeriod(
				jsonAbout.getInt("period"), 
				NHLGamePeriod.Type.parse(jsonAbout.getString("periodType")),
				jsonAbout.getString("ordinalNum"));
		periodTime = jsonAbout.getString("periodTime");
		team = NHLTeam.parse(jsonScoringPlays.getJSONObject("team").getInt("id"));

		JSONObject jsonResult = jsonScoringPlays.getJSONObject("result");
		strength = NHLGameEventStrength.parse(jsonResult.getJSONObject("strength").getString("code"));
		type = NHLGameEventType.parse(jsonResult.getString("eventTypeId"));

		JSONArray jsonPlayers = jsonScoringPlays.getJSONArray("players");
		for (int i = 0; i < jsonPlayers.length(); i++) {
			players.add(new NHLPlayer(jsonPlayers.getJSONObject(i)));
		}
	}

	public int getId() {
		return id;
	}

	public NHLGameEventType getType() {
		return type;
	}

	public NHLTeam getTeam() {
		return team;
	}

	public String getPeriodTime() {
		return periodTime;
	}

	public NHLGamePeriod getPeriod() {
		return period;
	}

	public List<NHLPlayer> getPlayers() {
		return players;
	}

	public NHLGameEventStrength getStrength() {
		return strength;
	}

	public Date getDate() {
		return date;
	}

	public int getIdx() {
		return idx;
	}

	@Override
	public String toString() {
		return "NHLGameEvent [id=" + id + ", idx=" + idx + ", date=" + date + ", type=" + type + ", team=" + team
				+ ", periodTime=" + periodTime + ", period=" + period + ", players=" + players + ", strength="
				+ strength + "]";
	}
}
