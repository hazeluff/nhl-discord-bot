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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + id;
		result = prime * result + idx;
		result = prime * result + ((period == null) ? 0 : period.hashCode());
		result = prime * result + ((periodTime == null) ? 0 : periodTime.hashCode());
		result = prime * result + ((players == null) ? 0 : players.hashCode());
		result = prime * result + ((strength == null) ? 0 : strength.hashCode());
		result = prime * result + ((team == null) ? 0 : team.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		NHLGameEvent other = (NHLGameEvent) obj;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (id != other.id)
			return false;
		if (idx != other.idx)
			return false;
		if (period == null) {
			if (other.period != null)
				return false;
		} else if (!period.equals(other.period))
			return false;
		if (periodTime == null) {
			if (other.periodTime != null)
				return false;
		} else if (!periodTime.equals(other.periodTime))
			return false;
		if (players == null) {
			if (other.players != null)
				return false;
		} else if (!players.equals(other.players))
			return false;
		if (strength != other.strength)
			return false;
		if (team != other.team)
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "NHLGameEvent [id=" + id + ", idx=" + idx + ", date=" + date + ", type=" + type + ", team=" + team
				+ ", periodTime=" + periodTime + ", period=" + period + ", players=" + players + ", strength="
				+ strength + "]";
	}
}
