package com.hazeluff.discord.nhlbot.nhl;

import java.util.HashMap;
import java.util.Map;

public enum Division {
	ATLANTIC(17, "Atlantic", Conference.EASTERN), 
	CENTRAL(16, "Central", Conference.WESTERN), 
	METRO(18, "Metropolitan", Conference.EASTERN), 
	PACIFIC(15, "Pacific", Conference.WESTERN);

	private final int id;
	private final String name;
	private final Conference conference;

	private static final Map<Integer, Division> VALUES_MAP = new HashMap<>();

	static {
		for (Division d : Division.values()) {
			VALUES_MAP.put(d.id, d);
		}
	}

	private Division(int id, String name, Conference conference) {
		this.id = id;
		this.name = name;
		this.conference = conference;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Conference getConference() {
		return conference;
	}

	public static Division parse(int id) {
		Division result = VALUES_MAP.get(id);
		if (result == null) {
			throw new IllegalArgumentException("No value exists for: " + id);
		}
		return result;
	}
}
