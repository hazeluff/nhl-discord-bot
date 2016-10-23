package com.hazeluff.discort.canucksbot.nhl;

import java.util.Date;

import org.json.JSONObject;

public class NHLGameEvent {
	private final int id;
	private final NHLTeam team;
	private final Date gameTime;
	private final int period;
	private final NHLPlayer players;
	private final String strength;

	public NHLGameEvent(JSONObject jsonScoringPlays) {
		id = 0;
		team = null;
		gameTime = null;
		period = 0;
		players = null;
		strength = null;

	}
}
