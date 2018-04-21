package com.hazeluff.discord.nhlbot.nhl;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public class Player {
	private final int id;
	private final String fullName;
	private final EventRole role;

	// role the player had in an event
	public enum EventRole {
		SCORER("Scorer"), ASSIST("Assist"), GOALIE("Goalie");

		private final String value;

		private static final Map<String, EventRole> VALUES_MAP = new HashMap<>();

		static {
			for (EventRole er : EventRole.values()) {
				VALUES_MAP.put(er.value, er);
			}
		}

		private EventRole(String value) {
			this.value = value;
		}

		public String toString() {
			return value;
		}

		public static EventRole parse(String value) {
			EventRole result = VALUES_MAP.get(value);
			if (result == null) {
				throw new IllegalArgumentException("No value exists for: " + value);
			}
			return result;
		}
	}

	Player(int id, String fullName, EventRole role) {
		this.id = id;
		this.fullName = fullName;
		this.role = role;
	}

	public static Player parse(JSONObject jsonPlayer) {
		return new Player(
				jsonPlayer.getJSONObject("player").getInt("id"), 
				jsonPlayer.getJSONObject("player").getString("fullName"), 
				EventRole.parse(jsonPlayer.getString("playerType")));
	}

	public int getId() {
		return id;
	}

	public String getFullName() {
		return fullName;
	}

	public EventRole getRole() {
		return role;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fullName == null) ? 0 : fullName.hashCode());
		result = prime * result + id;
		result = prime * result + ((role == null) ? 0 : role.hashCode());
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
		Player other = (Player) obj;
		if (fullName == null) {
			if (other.fullName != null)
				return false;
		} else if (!fullName.equals(other.fullName))
			return false;
		if (id != other.id)
			return false;
		if (role != other.role)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "NHLPlayer [id=" + id + ", fullName=" + fullName + ", role=" + role + "]";
	}
}
