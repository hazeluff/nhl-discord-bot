package com.hazeluff.discord.nhlbot.nhl;

import java.util.HashMap;
import java.util.Map;

public enum Conference {
	WESTERN(5, "Western"), EASTERN(6, "Eastern");
	
	private final int id;
	private final String name;
	
	private static final Map<Integer, Conference> VALUES_MAP = new HashMap<>();
	
    static {
        for (Conference c : Conference.values()) {
            VALUES_MAP.put(c.id, c);
        }
    }	
	
	private Conference(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public static Conference parse(int id) {
    	Conference result = VALUES_MAP.get(id);
        if (result == null) {
			throw new IllegalArgumentException("No value exists for: " + id);
        }
        return result;
    }
}
