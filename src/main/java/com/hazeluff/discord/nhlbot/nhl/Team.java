package com.hazeluff.discord.nhlbot.nhl;

import java.util.HashMap;
import java.util.Map;

public enum Team {
	NEW_JERSEY_DEVILS(1, "New Jersey", "Devils", "NJD", Division.METRO),
	NEW_YORK_ISLANDERS(2, "New York", "Islanders", "NYI", Division.METRO), 
	NEW_YORK_RANGERS(3, "New York", "Rangers", "NYR", Division.METRO),
	PHILADELPHIA_FLYERS(4, "Philadelphia", "Flyers", "PHI", Division.METRO),
	PITTSBURGH_PENGUINS(5, "Pittsburgh", "Penguins", "PIT", Division.METRO),
	BOSTON_BRUINS(6, "Boston", "Bruins", "BOS", Division.ATLANTIC),
	BUFFALO_SABRES(7, "Buffalo", "Sabres", "BUF", Division.ATLANTIC),
	MONTREAL_CANADIENS(8, "Montréal", "Canadiens", "MTL", Division.ATLANTIC),
	OTTAWA_SENATORS(9, "Ottawa", "Senators", "OTT", Division.ATLANTIC),
	TORONTO_MAPLE_LEAFS(10, "Toronto", "Maple Leafs", "TOR", Division.ATLANTIC),
	CAROLINA_HURRICANES(12, "Carolina", "Hurricanes", "CAR", Division.METRO),
	FLORIDA_PANTHERS(13, "Florida", "Panthers", "FLA", Division.ATLANTIC),
	TAMPA_BAY_LIGHTNING(14, "Tampa Bay", "Lightning", "TBL", Division.ATLANTIC),
	WASHINGTON_CAPITALS(15, "Washington", "Capitals", "WSH", Division.METRO),
	CHICAGO_BLACKHAWKS(16, "Chicago", "Blackhawks", "CHI", Division.CENTRAL),
	DETROIT_RED_WINGS(17, "Detroit", "Red Wings", "DET", Division.ATLANTIC),
	NASHVILLE_PREDATORS(18, "Nashville", "Predators", "NSH", Division.CENTRAL),
	ST_LOUIS_BLUES(19, "St. Louis", "Blues", "STL", Division.CENTRAL),
	CALGARY_FLAMES(20, "Calgary", "Flames", "CGY", Division.PACIFIC),
	COLORADO_AVALANCH(21, "Colorado", "Avalanche", "COL", Division.CENTRAL),
	EDMONTON_OILERS(22, "Edmonton", "Oilers", "EDM", Division.PACIFIC),
	VANCOUVER_CANUCKS(23, "Vancouver", "Canucks", "VAN", Division.PACIFIC),
	ANAHEIM_DUCKS(24, "Anaheim", "Ducks", "ANA", Division.PACIFIC),
	DALLAS_STARS(25, "Dallas", "Stars", "DAL", Division.CENTRAL),
	LA_KINGS(26, "Los Angeles", "Kings", "LAK", Division.PACIFIC),
	SAN_JOSE_SHARKS(28, "San Jose", "Sharks", "SJS", Division.PACIFIC),
	COLUMBUS_BLUE_JACKETS(29, "Columbus", "Blue Jackets", "CBJ", Division.METRO),
	MINNESOTA_WILD(30, "Minnesota", "Wild", "MIN", Division.CENTRAL),
	WINNIPEG_JETS(52, "Winnipeg", "Jets", "WPG", Division.CENTRAL),
	ARIZONA_COYOTES(53, "Arizona", "Coyotes", "ARI", Division.PACIFIC);
	

	private final int id;
	private final String location;
	private final String name;
	private final String code;
	private final Division division;

	private static final Map<Integer, Team> VALUES_MAP = new HashMap<>();

	static {
		for (Team t : Team.values()) {
			VALUES_MAP.put(t.id, t);
		}
	}

	private Team(int id, String location, String name, String code, Division division) {
		this.id = id;
		this.location = location;
		this.name = name;
		this.code = code;
		this.division = division;
	}

	public int getId() {
		return id;
	}

	public String getLocation() {
		return location;
	}

	public String getName() {
		return name;
	}

	public String getFullName() {
		return location + " " + name;
	}

	public String getCode() {
		return code;
	}

	public Division getDivision() {
		return division;
	}

	public static Team parse(int id) {
		Team result = VALUES_MAP.get(id);
		if (result == null) {
			throw new IllegalArgumentException("No value exists for: " + id);
		}
		return result;
	}
}
