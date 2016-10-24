package com.hazeluff.discord.canucksbot.nhl;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public class NHLPlayer {
	private final int id;
	private final String fullName;
	private final EventRole role;

	// role the player had in an event
	public enum EventRole {
		SCORER("Scorer"), ASSIST("Assist");

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

		public static EventRole parse(String value) {
			EventRole result = VALUES_MAP.get(value);
			if (result == null) {
				throw new IllegalArgumentException("No value exists for: " + value);
			}
			return result;
		}
	}

	public NHLPlayer(JSONObject jsonPlayer) {
		this.id = jsonPlayer.getJSONObject("player").getInt("id");
		this.fullName = jsonPlayer.getJSONObject("player").getString("fullName");
		this.role = EventRole.parse(jsonPlayer.getString("playerType"));
	}

	public String getFullName() {
		return fullName;
	}

	public EventRole getRole() {
		return role;
	}

	@Override
	public String toString() {
		return "NHLPlayer [id=" + id + ", fullName=" + fullName + ", role=" + role + "]";
	}
}
