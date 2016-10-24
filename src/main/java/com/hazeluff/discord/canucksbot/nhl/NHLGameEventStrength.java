package com.hazeluff.discord.canucksbot.nhl;

import java.util.HashMap;
import java.util.Map;


public enum NHLGameEventStrength {
	EVEN("EVEN", "Even"), PPG("PPG", "Power Play"), SHORTHANDED("SHG", "Short Handed");

	private final String id;
	private final String value;

	private static final Map<String, NHLGameEventStrength> VALUES_MAP = new HashMap<>();

	static {
		for (NHLGameEventStrength s : NHLGameEventStrength.values()) {
			VALUES_MAP.put(s.id, s);
		}
	}

	private NHLGameEventStrength(String id, String value) {
		this.id = id;
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static NHLGameEventStrength parse(String id) {
		NHLGameEventStrength result = VALUES_MAP.get(id);
		if (result == null) {
			throw new IllegalArgumentException("No value exists for: " + id);
		}
		return result;
	}
}