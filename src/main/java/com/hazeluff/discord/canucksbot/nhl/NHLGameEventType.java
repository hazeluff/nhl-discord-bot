package com.hazeluff.discord.canucksbot.nhl;

import java.util.HashMap;
import java.util.Map;

public enum NHLGameEventType {
	GOAL("GOAL", "Goal");

	private final String id;
	private final String value;

	private static final Map<String, NHLGameEventType> VALUES_MAP = new HashMap<>();

	static {
		for (NHLGameEventType t : NHLGameEventType.values()) {
			VALUES_MAP.put(t.id, t);
		}
	}

	private NHLGameEventType(String id, String value) {
		this.id = id;
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static NHLGameEventType parse(String id) {
		NHLGameEventType result = VALUES_MAP.get(id);
		if (result == null) {
			throw new IllegalArgumentException("No value exists for: " + id);
		}
		return result;
	}
}

