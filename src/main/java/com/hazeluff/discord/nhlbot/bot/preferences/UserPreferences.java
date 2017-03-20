package com.hazeluff.discord.nhlbot.bot.preferences;

import com.hazeluff.discord.nhlbot.nhl.Team;

public class UserPreferences {
	private Team team;

	public UserPreferences() {}

	public UserPreferences(Team team) {
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
		UserPreferences other = (UserPreferences) obj;
		if (team != other.team)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "UserPreferences [team=" + team + "]";
	}
}
