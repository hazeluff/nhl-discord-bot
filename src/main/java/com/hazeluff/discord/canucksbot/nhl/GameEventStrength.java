package com.hazeluff.discord.canucksbot.nhl;

import java.util.HashMap;
import java.util.Map;


public enum GameEventStrength {
	EVEN("EVEN", "Even"), PPG("PPG", "Power Play"), SHORTHANDED("SHG", "Short Handed");

	private final String id;
	private final String value;

	private static final Map<String, GameEventStrength> VALUES_MAP = new HashMap<>();

	static {
		for (GameEventStrength s : GameEventStrength.values()) {
			VALUES_MAP.put(s.id, s);
		}
	}

	private GameEventStrength(String id, String value) {
		this.id = id;
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static GameEventStrength parse(String id) {
		GameEventStrength result = VALUES_MAP.get(id);
		if (result == null) {
			throw new IllegalArgumentException("No value exists for: " + id);
		}
		return result;
	}
}