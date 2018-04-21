package com.hazeluff.discord.nhlbot.nhl;

import java.util.HashMap;
import java.util.Map;

public enum GameEventType {
	GOAL("GOAL", "Goal");

	private final String id;
	private final String value;

	private static final Map<String, GameEventType> VALUES_MAP = new HashMap<>();

	static {
		for (GameEventType t : GameEventType.values()) {
			VALUES_MAP.put(t.id, t);
		}
	}

	private GameEventType(String id, String value) {
		this.id = id;
		this.value = value;
	}

	public String getId() {
		return id;
	}

	public String getValue() {
		return value;
	}

	public static GameEventType parse(String id) {
		GameEventType result = VALUES_MAP.get(id);
		if (result == null) {
			throw new IllegalArgumentException("No value exists for: " + id);
		}
		return result;
	}
}

