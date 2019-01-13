package com.hazeluff.discord.nhlbot.bot.preferences;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hazeluff.discord.nhlbot.nhl.Team;

public class GuildPreferences {
	private Set<Team> teams;

	public GuildPreferences() {
		this.teams = new HashSet<>();
	}

	public GuildPreferences(Set<Team> teams) {
		this.teams = teams;
	}

	public List<Team> getTeams() {
		return new ArrayList<>(teams);
	}

	public void addTeam(Team team) {
		teams.add(team);
	}

	public void removeTeam(Team team) {
		teams.remove(team);
	}

	public String getCheer() {
		if (teams.size() > 1) {
			return Team.MULTI_TEAM_CHEER;
		} else {
			Team team = teams.iterator().next();
			return team == null ? Team.MULTI_TEAM_CHEER : team.getCheer();
		}
	}

	public ZoneId getTimeZone() {
		if (teams.size() > 1) {
			return ZoneId.of("America/Toronto");
		} else {
			return teams.iterator().next().getTimeZone();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((teams == null) ? 0 : teams.hashCode());
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
		GuildPreferences other = (GuildPreferences) obj;
		if (teams == null) {
			if (other.teams != null)
				return false;
		} else if (!teams.equals(other.teams))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "GuildPreferences [teams=" + teams + "]";
	}

}
