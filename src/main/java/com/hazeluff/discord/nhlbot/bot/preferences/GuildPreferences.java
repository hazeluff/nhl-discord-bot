package com.hazeluff.discord.nhlbot.bot.preferences;

import com.hazeluff.discord.nhlbot.nhl.Team;

public class GuildPreferences {
	private Team team;

	public GuildPreferences() {}

	public GuildPreferences(Team team) {
		this.team = team;
	}

	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((team == null) ? 0 : team.hashCode());
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
		if (team != other.team)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "GuildPreference [team=" + team + "]";
	}
}
