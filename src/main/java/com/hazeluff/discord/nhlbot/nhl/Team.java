package com.hazeluff.discord.nhlbot.nhl;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

public enum Team {
	NEW_JERSEY_DEVILS(1, "New Jersey", "Devils", "NJD", Division.METRO, "Lets Go Devils!", 
			ZoneId.of("America/New_York")),
	NEW_YORK_ISLANDERS(2, "New York", "Islanders", "NYI", Division.METRO, "Lets Go Islanders!",
			ZoneId.of("America/New_York")), 
	NEW_YORK_RANGERS(3, "New York", "Rangers", "NYR", Division.METRO, "Lets Go Rangers!",
			ZoneId.of("America/New_York")),
	PHILADELPHIA_FLYERS(4, "Philadelphia", "Flyers", "PHI", Division.METRO, "Lets Go Flyers!", 
			ZoneId.of("America/New_York")),
	PITTSBURGH_PENGUINS(5, "Pittsburgh", "Penguins", "PIT", Division.METRO, "Lets Go Pens!", 
			ZoneId.of("America/New_York")),
	BOSTON_BRUINS(6, "Boston", "Bruins", "BOS", Division.ATLANTIC, "Lets Go Bruins!", 
			ZoneId.of("America/New_York")),
	BUFFALO_SABRES(7, "Buffalo", "Sabres", "BUF", Division.ATLANTIC, "Lets Go Buffalo!", 
			ZoneId.of("America/New_York")),
	MONTREAL_CANADIENS(8, "Montréal", "Canadiens", "MTL", Division.ATLANTIC, "Olé Olé Olé",
			ZoneId.of("America/Montreal")),
	OTTAWA_SENATORS(9, "Ottawa", "Senators", "OTT", Division.ATLANTIC, "Go Sens Go!", 
			ZoneId.of("America/Toronto")),
	TORONTO_MAPLE_LEAFS(10, "Toronto", "Maple Leafs", "TOR", Division.ATLANTIC, "Go Leafs Go!",
			ZoneId.of("America/Toronto")),
	CAROLINA_HURRICANES(12, "Carolina", "Hurricanes", "CAR", Division.METRO, "Lets Go Canes!", 
			ZoneId.of("America/New_York")),
	FLORIDA_PANTHERS(13, "Florida", "Panthers", "FLA", Division.ATLANTIC, "Lets Go Panthers!", 
			ZoneId.of("America/New_York")),
	TAMPA_BAY_LIGHTNING(14, "Tampa Bay", "Lightning", "TBL", Division.ATLANTIC, "Lets Go Lightning!", 
			ZoneId.of("America/New_York")),
	WASHINGTON_CAPITALS(15, "Washington", "Capitals", "WSH", Division.METRO, "Lets Go Caps!", 
			ZoneId.of("America/New_York")),
	CHICAGO_BLACKHAWKS(16, "Chicago", "Blackhawks", "CHI", Division.CENTRAL, "Lets Go Hawks!",
			ZoneId.of("America/Chicago")),
	DETROIT_RED_WINGS(17, "Detroit", "Red Wings", "DET", Division.ATLANTIC, "Lets Go Red Wings!",
			ZoneId.of("America/Detroit")),
	NASHVILLE_PREDATORS(18, "Nashville", "Predators", "NSH", Division.CENTRAL, "Lets Go Predators!", 
			ZoneId.of("America/Chicago")),
	ST_LOUIS_BLUES(19, "St. Louis", "Blues", "STL", Division.CENTRAL, "Lets Go Blues!",
			ZoneId.of("America/Chicago")),
	CALGARY_FLAMES(20, "Calgary", "Flames", "CGY", Division.PACIFIC, "Go Flames Go!", 
			ZoneId.of("America/Edmonton")),
	COLORADO_AVALANCH(21, "Colorado", "Avalanche", "COL", Division.CENTRAL, "Lets Go Colorado!",
			ZoneId.of("America/Denver")),
	EDMONTON_OILERS(22, "Edmonton", "Oilers", "EDM", Division.PACIFIC, "Let go Oilers!", 
			ZoneId.of("America/Edmonton")),
	VANCOUVER_CANUCKS(23, "Vancouver", "Canucks", "VAN", Division.PACIFIC, "Go Canucks Go!",
			ZoneId.of("America/Vancouver")),
	ANAHEIM_DUCKS(24, "Anaheim", "Ducks", "ANA", Division.PACIFIC, "Lets Go Ducks!",
			ZoneId.of("America/Los_Angeles")),
	DALLAS_STARS(25, "Dallas", "Stars", "DAL", Division.CENTRAL, "Go Stars Go!", 
			ZoneId.of("America/Chicago")),
	LA_KINGS(26, "Los Angeles", "Kings", "LAK", Division.PACIFIC, "Go Kings Go!", 
			ZoneId.of("America/Los_Angeles")),
	SAN_JOSE_SHARKS(28, "San Jose", "Sharks", "SJS", Division.PACIFIC, "Lets Go Sharks!",
			ZoneId.of("America/Los_Angeles")),
	COLUMBUS_BLUE_JACKETS(29, "Columbus", "Blue Jackets", "CBJ", Division.METRO, "CBJ! CBJ!", 
			ZoneId.of("America/Chicago")),
	MINNESOTA_WILD(30, "Minnesota", "Wild", "MIN", Division.CENTRAL, "Lets Go Wild!", 
			ZoneId.of("America/Chicago")),
	WINNIPEG_JETS(52, "Winnipeg", "Jets", "WPG", Division.CENTRAL, "Go Jets Go!", 
			ZoneId.of("America/Winnipeg")),
	ARIZONA_COYOTES(53, "Arizona", "Coyotes", "ARI", Division.PACIFIC, "Lets Go Coyotes!", 
			ZoneId.of("America/Denver")),
	VEGAS_GOLDEN_KNIGHTS(54, "Vegas", "Golden Knights", "VGK", Division.PACIFIC, "Go Knights Go!",
			ZoneId.of("America/Los_Angeles"));
	

	private final int id;
	private final String location;
	private final String name;
	private final String code;
	private final Division division;
	private final String cheer;
	private final ZoneId timeZone;

	private static final Map<Integer, Team> VALUES_MAP = new HashMap<>();
	private static final Map<String, Team> CODES_MAP = new HashMap<>();

	static {
		for (Team t : Team.values()) {
			VALUES_MAP.put(t.id, t);
		}
		for (Team t : Team.values()) {
			CODES_MAP.put(t.code, t);
		}
	}

	private Team(int id, String location, String name, String code, Division division, String cheer, ZoneId timeZone) {
		this.id = id;
		this.location = location;
		this.name = name;
		this.code = code;
		this.division = division;
		this.cheer = cheer;
		this.timeZone = timeZone;
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

	public String getCheer() {
		return cheer;
	}

	public ZoneId getTimeZone() {
		return timeZone;
	}

	public static Team parse(Integer id) {
		if (id == null) {
			return null;
		}
		Team result = VALUES_MAP.get(id);
		if (result == null) {
			throw new IllegalArgumentException("No value exists for: " + id);
		}
		return result;
	}

	public static boolean isValid(String code) {
		return CODES_MAP.get(code.toUpperCase()) != null;
	}

	/**
	 * Parse's a team's code into a Team object
	 * 
	 * @param code
	 *            code of the team
	 * @return
	 */
	public static Team parse(String code) {
		if (code == null || code.isEmpty()) {
			return null;
		}
		Team result = CODES_MAP.get(code.toUpperCase());
		if (result == null) {
			throw new IllegalArgumentException("No value exists for: " + code);
		}
		return result;
	}
}
