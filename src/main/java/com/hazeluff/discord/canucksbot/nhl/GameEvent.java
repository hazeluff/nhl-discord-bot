package com.hazeluff.discord.canucksbot.nhl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.hazeluff.discord.canucksbot.nhl.Player.EventRole;
import com.hazeluff.discord.canucksbot.utils.DateUtils;

public class GameEvent {
	private final int id;
	private int idx;
	private LocalDateTime date;
	private final GameEventType type;
	private final Team team;
	private final String periodTime;
	private final GamePeriod period;
	private final List<Player> players = new ArrayList<>();
	private final GameEventStrength strength;

	public GameEvent(JSONObject jsonScoringPlay) {
		JSONObject jsonAbout = jsonScoringPlay.getJSONObject("about");
		id = jsonAbout.getInt("eventId");
		idx = jsonAbout.getInt("eventIdx");
		date = DateUtils.parseNHLDate(jsonAbout.getString("dateTime"));
		period = new GamePeriod(
				jsonAbout.getInt("period"), 
				GamePeriod.Type.parse(jsonAbout.getString("periodType")),
				jsonAbout.getString("ordinalNum"));
		periodTime = jsonAbout.getString("periodTime");
		team = Team.parse(jsonScoringPlay.getJSONObject("team").getInt("id"));

		JSONObject jsonResult = jsonScoringPlay.getJSONObject("result");
		strength = GameEventStrength.parse(jsonResult.getJSONObject("strength").getString("code"));
		type = GameEventType.parse(jsonResult.getString("eventTypeId"));

		JSONArray jsonPlayers = jsonScoringPlay.getJSONArray("players");
		for (int i = 0; i < jsonPlayers.length(); i++) {
			players.add(new Player(jsonPlayers.getJSONObject(i)));
		}
		
		players.removeIf(player -> player.getRole() == EventRole.GOALIE);
	}

	GameEvent(int id, int idx, LocalDateTime date, GameEventType type, Team team, String periodTime, GamePeriod period,
			List<Player> players, GameEventStrength strength) {
		this.id = id;
		this.idx = idx;
		this.date = date;
		this.type = type;
		this.team = team;
		this.periodTime = periodTime;
		this.period = period;
		this.players.addAll(players);
		this.strength = strength;
	}

	public int getId() {
		return id;
	}

	public GameEventType getType() {
		return type;
	}

	public Team getTeam() {
		return team;
	}

	public String getPeriodTime() {
		return periodTime;
	}

	public GamePeriod getPeriod() {
		return period;
	}

	public List<Player> getPlayers() {
		return players;
	}

	public GameEventStrength getStrength() {
		return strength;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public int getIdx() {
		return idx;
	}

	/**
	 * Builds the details to be displayed.
	 * 
	 * @return details as formatted string
	 */
	public String getDetails() {
		StringBuilder details = new StringBuilder();
		details.append(String.format("%s - %s %-18s", periodTime, team.getCode(), players.get(0).getFullName()));
		if (players.size() > 1) {
			details.append("  Assists: ");
			details.append(players.get(1).getFullName());
		}
		if (players.size() > 2) {
			details.append(", ");
			details.append(players.get(2).getFullName());
		}
		return details.toString();
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
		GameEvent other = (GameEvent) obj;
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
