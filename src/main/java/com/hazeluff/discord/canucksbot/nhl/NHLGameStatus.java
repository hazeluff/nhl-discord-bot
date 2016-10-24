package com.hazeluff.discord.canucksbot.nhl;

import java.util.HashMap;
import java.util.Map;

public enum NHLGameStatus {
	PREVIEW(new int[] { 1 }, "Pre-game"), 
	STARTED(new int[] { 2, 4 }, "Started"), // I have no idea what these codes translate to
	LIVE(new int[] { 3 }, "LIVE"),
	FINAL(new int[] { 5, 6, 7 }, "Final");
	
	private final int[] ids;
	private final String value;

	private static final Map<Integer, NHLGameStatus> VALUES_MAP = new HashMap<>();

	static {
		for (NHLGameStatus d : NHLGameStatus.values()) {
			for (int id : d.ids) {
				VALUES_MAP.put(id, d);				
			}
		}
	}

	private NHLGameStatus(int[] ids, String value) {
		this.ids = ids;
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static NHLGameStatus parse(int id) {
		NHLGameStatus result = VALUES_MAP.get(id);
		if (result == null) {
			throw new IllegalArgumentException("No value exists for: " + id);
		}
		return result;
	}
}
