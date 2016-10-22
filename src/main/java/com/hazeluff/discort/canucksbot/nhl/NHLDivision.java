package com.hazeluff.discort.canucksbot.nhl;

import java.util.HashMap;
import java.util.Map;

public enum NHLDivision {
	ATLANTIC(17, "Atlantic", NHLConference.EASTERN), 
	CENTRAL(16, "Central", NHLConference.WESTERN), 
	METRO(18, "Metropolitan", NHLConference.EASTERN), 
	PACIFIC(15, "Pacific", NHLConference.WESTERN);

	public final int id;
	public final String name;
	public final NHLConference conference;

	private static final Map<Integer, NHLDivision> VALUES_MAP = new HashMap<>();

	static {
		for (NHLDivision d : NHLDivision.values()) {
			VALUES_MAP.put(d.id, d);
		}
	}

	private NHLDivision(int id, String name, NHLConference conference) {
		this.id = id;
		this.name = name;
		this.conference = conference;
	}

	public static NHLDivision parse(int id) {
		NHLDivision result = VALUES_MAP.get(id);
		if (result == null) {
			throw new IllegalArgumentException("No value exists for: " + id);
		}
		return result;
	}
}
