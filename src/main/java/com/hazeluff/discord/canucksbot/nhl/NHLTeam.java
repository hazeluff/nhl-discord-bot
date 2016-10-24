package com.hazeluff.discord.canucksbot.nhl;

import java.util.HashMap;
import java.util.Map;

public enum NHLTeam {
	NEW_JERSEY_DEVILS(1, "New Jersey", "Devils", "NJD", NHLDivision.METRO),
	NEW_YORK_ISLANDERS(2, "New York", "Islanders", "NYI", NHLDivision.METRO), 
	NEW_YORK_RANGERS(3, "New York", "Rangers", "NYR", NHLDivision.METRO),
	PHILADELPHIA_FLYERS(4, "Philadelphia", "Flyers", "PHI", NHLDivision.METRO),
	PITTSBURGH_PENGUINS(5, "Pittsburgh", "Penguins", "PIT", NHLDivision.METRO),
	BOSTON_BRUINS(6, "Boston", "Bruins", "BOS", NHLDivision.ATLANTIC),
	BUFFALO_SABRES(7, "Buffalo", "Sabres", "BUF", NHLDivision.ATLANTIC),
	MONTREAL_CANADIENS(8, "Montréal", "Canadiens", "MTL", NHLDivision.ATLANTIC),
	OTTAWA_SENATORS(9, "Ottawa", "Senators", "OTT", NHLDivision.ATLANTIC),
	TORONTO_MAPLE_LEAFS(10, "Toronto", "Maple Leafs", "TOR", NHLDivision.ATLANTIC),
	CAROLINA_HURRICANES(12, "Carolina", "Hurricanes", "CAR", NHLDivision.METRO),
	FLORIDA_PANTHERS(13, "Florida", "Panthers", "FLA", NHLDivision.ATLANTIC),
	TAMPA_BAY_LIGHTNING(14, "Tampa Bay", "Lightning", "TBL", NHLDivision.ATLANTIC),
	WASHINGTON_CAPITALS(15, "Washington", "Capitals", "WSH", NHLDivision.METRO),
	CHICAGO_BLACKHAWKS(16, "Chicago", "Blackhawks", "CHI", NHLDivision.CENTRAL),
	DETROIT_RED_WINGS(17, "Detroit", "Red Wings", "DET", NHLDivision.ATLANTIC),
	NASHVILLE_PREDATORS(18, "Nashville", "Predators", "NSH", NHLDivision.CENTRAL),
	ST_LOUIS_BLUES(19, "St. Louis", "Blues", "STL", NHLDivision.CENTRAL),
	CALGARY_FLAMES(20, "Calgary", "Flames", "CGY", NHLDivision.PACIFIC),
	COLORADO_AVALANCH(21, "Colorado", "Avalanche", "COL", NHLDivision.CENTRAL),
	EDMONTON_OILERS(22, "Edmonton", "Oilers", "EDM", NHLDivision.PACIFIC),
	VANCOUVER_CANUCKS(23, "Vancouver", "Canucks", "VAN", NHLDivision.PACIFIC),
	ANAHEIM_DUCKS(24, "Anaheim", "Ducks", "ANA", NHLDivision.PACIFIC),
	DALLAS_STARS(25, "Dallas", "Stars", "DAL", NHLDivision.CENTRAL),
	LA_KINGS(26, "Los Angeles", "Kings", "LAK", NHLDivision.PACIFIC),
	SAN_JOSE_SHARKS(28, "San Jose", "Sharks", "SJS", NHLDivision.PACIFIC),
	COLUMBUS_BLUE_JACKETS(29, "Columbus", "Blue Jackets", "CBJ", NHLDivision.METRO),
	MINNESOTA_WILD(30, "Minnesota", "Wild", "MIN", NHLDivision.CENTRAL),
	WINNIPEG_JETS(52, "Winnipeg", "Jets", "WPG", NHLDivision.CENTRAL),
	ARIZONA_COYOTES(53, "Arizona", "Coyotes", "ARI", NHLDivision.PACIFIC);
	

	private final int id;
	private final String location;
	private final String name;
	private final String code;
	private final NHLDivision division;

	private static final Map<Integer, NHLTeam> VALUES_MAP = new HashMap<>();

	static {
		for (NHLTeam t : NHLTeam.values()) {
			VALUES_MAP.put(t.id, t);
		}
	}

	private NHLTeam(int id, String location, String name, String code, NHLDivision division) {
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

	public NHLDivision getDivision() {
		return division;
	}

	public static NHLTeam parse(int id) {
		NHLTeam result = VALUES_MAP.get(id);
		if (result == null) {
			throw new IllegalArgumentException("No value exists for: " + id);
		}
		return result;
	}
}
