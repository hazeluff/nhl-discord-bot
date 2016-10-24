package com.hazeluff.discord.canucksbot.nhl;

import java.util.HashMap;
import java.util.Map;

public enum NHLConference {
	WESTERN(5, "Western"), EASTERN(6, "Eastern");
	
	private final int id;
	private final String name;
	
	private static final Map<Integer, NHLConference> VALUES_MAP = new HashMap<>();
	
    static {
        for (NHLConference c : NHLConference.values()) {
            VALUES_MAP.put(c.id, c);
        }
    }	
	
	private NHLConference(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public static NHLConference parse(int id) {
    	NHLConference result = VALUES_MAP.get(id);
        if (result == null) {
			throw new IllegalArgumentException("No value exists for: " + id);
        }
        return result;
    }
}
