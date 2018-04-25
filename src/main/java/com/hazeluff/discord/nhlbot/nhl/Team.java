package com.hazeluff.discord.nhlbot.nhl;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

public enum Team {
	NEW_JERSEY_DEVILS(
			1, 
			"New Jersey", "Devils", 
			"NJD", 
			Division.METRO, 
			"Lets Go Devils!",
			0xC8102E,
			ZoneId.of("America/New_York")),
	NEW_YORK_ISLANDERS(
			2, 
			"New York", 
			"Islanders", 
			"NYI", 
			Division.METRO, 
			"Lets Go Islanders!",
			0xF26924,
			ZoneId.of("America/New_York")), 
	NEW_YORK_RANGERS(
			3, 
			"New York", 
			"Rangers", 
			"NYR", 
			Division.METRO, 
			"Lets Go Rangers!",
			0x0038A8,
			ZoneId.of("America/New_York")),
	PHILADELPHIA_FLYERS(
			4, 
			"Philadelphia", 
			"Flyers", 
			"PHI", 
			Division.METRO, 
			"Lets Go Flyers!",
			0xFA4616,
			ZoneId.of("America/New_York")),
	PITTSBURGH_PENGUINS(
			5, 
			"Pittsburgh", 
			"Penguins", 
			"PIT", 
			Division.METRO, 
			"Lets Go Pens!", 
			0xFFB81C,
			ZoneId.of("America/New_York")),
	BOSTON_BRUINS(
			6, 
			"Boston", 
			"Bruins", 
			"BOS", 
			Division.ATLANTIC, 
			"Lets Go Bruins!", 
			0xFFB81C,
			ZoneId.of("America/New_York")),
	BUFFALO_SABRES(
			7, 
			"Buffalo", 
			"Sabres", 
			"BUF", 
			Division.ATLANTIC, 
			"Lets Go Buffalo!", 
			0xFFB81C,
			ZoneId.of("America/New_York")),
	MONTREAL_CANADIENS(
			8, 
			"Montréal", 
			"Canadiens", 
			"MTL", 
			Division.ATLANTIC, 
			"Olé Olé Olé",
			0xA6192E,
			ZoneId.of("America/Montreal")),
	OTTAWA_SENATORS(
			9, 
			"Ottawa", 
			"Senators", 
			"OTT", 
			Division.ATLANTIC, 
			"Go Sens Go!", 
			0xC8102E,
			ZoneId.of("America/Toronto")),
	TORONTO_MAPLE_LEAFS(
			10, 
			"Toronto", 
			"Maple Leafs", 
			"TOR", 
			Division.ATLANTIC, 
			"Go Leafs Go!",
			0x00205B,
			ZoneId.of("America/Toronto")),
	CAROLINA_HURRICANES(
			12, 
			"Carolina", 
			"Hurricanes", 
			"CAR", 
			Division.METRO, 
			"Lets Go Canes!", 
			0xCC0000,
			ZoneId.of("America/New_York")),
	FLORIDA_PANTHERS(
			13, 
			"Florida", 
			"Panthers", 
			"FLA", 
			Division.ATLANTIC, 
			"Lets Go Panthers!", 
			0xB9975B,
			ZoneId.of("America/New_York")),
	TAMPA_BAY_LIGHTNING(
			14, 
			"Tampa Bay", 
			"Lightning", 
			"TBL", 
			Division.ATLANTIC, 
			"Lets Go Lightning!", 
			0x00205B,
			ZoneId.of("America/New_York")),
	WASHINGTON_CAPITALS(
			15, 
			"Washington", 
			"Capitals", 
			"WSH", 
			Division.METRO, 
			"Lets Go Caps!", 
			0xC8102E,
			ZoneId.of("America/New_York")),
	CHICAGO_BLACKHAWKS(
			16, 
			"Chicago", 
			"Blackhawks", 
			"CHI", 
			Division.CENTRAL, 
			"Lets Go Hawks!",
			0xC8102E,
			ZoneId.of("America/Chicago")),
	DETROIT_RED_WINGS(
			17, 
			"Detroit", 
			"Red Wings",
			"DET",
			Division.ATLANTIC,
			"Lets Go Red Wings!",
			0xC8102E,
			ZoneId.of("America/Detroit")),
	NASHVILLE_PREDATORS(
			18, 
			"Nashville", 
			"Predators", 
			"NSH", 
			Division.CENTRAL, 
			"Lets Go Predators!", 
			0xFFB81C,
			ZoneId.of("America/Chicago")),
	ST_LOUIS_BLUES(
			19, 
			"St. Louis", 
			"Blues", 
			"STL", 
			Division.CENTRAL, 
			"Lets Go Blues!",
			0x003087,
			ZoneId.of("America/Chicago")),
	CALGARY_FLAMES(
			20, 
			"Calgary", 
			"Flames", 
			"CGY", 
			Division.PACIFIC, 
			"Go Flames Go!", 
			0xC8102E,
			ZoneId.of("America/Edmonton")),
	COLORADO_AVALANCH(
			21, 
			"Colorado", 
			"Avalanche", 
			"COL", 
			Division.CENTRAL, 
			"Lets Go Colorado!",
			0x6F263D,
			ZoneId.of("America/Denver")),
	EDMONTON_OILERS(
			22, 
			"Edmonton",
			"Oilers",
			"EDM",
			Division.PACIFIC,
			"Let go Oilers!", 
			0xFC4C02,
			ZoneId.of("America/Edmonton")),
	VANCOUVER_CANUCKS(
			23, 
			"Vancouver", 
			"Canucks", 
			"VAN", 
			Division.PACIFIC, 
			"Go Canucks Go!",
			0x00843D,
			ZoneId.of("America/Vancouver")),
	ANAHEIM_DUCKS(
			24, 
			"Anaheim", 
			"Ducks", 
			"ANA", 
			Division.PACIFIC, 
			"Lets Go Ducks!",
			0xFC4C02,
			ZoneId.of("America/Los_Angeles")),
	DALLAS_STARS(
			25, 
			"Dallas",
			"Stars",
			"DAL",
			Division.CENTRAL, 
			"Go Stars Go!", 
			0x006341,
			ZoneId.of("America/Chicago")),
	LA_KINGS(
			26, 
			"Los Angeles",
			"Kings", 
			"LAK",
			Division.PACIFIC,
			"Go Kings Go!", 
			0x000000,
			ZoneId.of("America/Los_Angeles")),
	SAN_JOSE_SHARKS(
			28, 
			"San Jose", 
			"Sharks",
			"SJS", 
			Division.PACIFIC, 
			"Lets Go Sharks!",
			0x006272,
			ZoneId.of("America/Los_Angeles")),
	COLUMBUS_BLUE_JACKETS(
			29, 
			"Columbus", 
			"Blue Jackets", 
			"CBJ", 
			Division.METRO, 
			"C B J! C B J!", // WTF Seriously?
			0x041E42,
			ZoneId.of("America/Chicago")),
	MINNESOTA_WILD(
			30, 
			"Minnesota",
			"Wild", 
			"MIN", 
			Division.CENTRAL, 
			"Lets Go Wild!", 
			0x154734,
			ZoneId.of("America/Chicago")),
	WINNIPEG_JETS(
			52, 
			"Winnipeg", 
			"Jets", 
			"WPG", 
			Division.CENTRAL, 
			"Go Jets Go!", 
			0x041E42,
			ZoneId.of("America/Winnipeg")),
	ARIZONA_COYOTES(
			53, 
			"Arizona", 
			"Coyotes", 
			"ARI", 
			Division.PACIFIC, 
			"Lets Go Coyotes!", 
			0x8C2633,
			ZoneId.of("America/Denver")),
	VEGAS_GOLDEN_KNIGHTS(
			54, 
			"Vegas", 
			"Golden Knights", 
			"VGK", 
			Division.PACIFIC, 
			"Go Knights Go!",
			0xB9975B,
			ZoneId.of("America/Los_Angeles"));
	

	private final int id;
	private final String location;
	private final String name;
	private final String code;
	private final Division division;
	private final String cheer;
	private final int color;
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

	private Team(int id, String location, String name, String code, Division division, String cheer, int color,
			ZoneId timeZone) {
		this.id = id;
		this.location = location;
		this.name = name;
		this.code = code;
		this.division = division;
		this.cheer = cheer;
		this.color = color;
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

	public int getColor() {
		return color;
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
