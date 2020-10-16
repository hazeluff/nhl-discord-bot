package com.hazeluff.discord.nhl;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import discord4j.rest.util.Color;

public enum Team {
	NEW_JERSEY_DEVILS(
			1, 
			"New Jersey", "Devils", 
			"NJD", 
			Division.METRO, 
			"Lets Go Devils!",
			Color.of(0xC8102E),
			ZoneId.of("America/New_York")),
	NEW_YORK_ISLANDERS(
			2, 
			"New York", 
			"Islanders", 
			"NYI", 
			Division.METRO, 
			"Lets Go Islanders!",
			Color.of(0xF26924),
			ZoneId.of("America/New_York")), 
	NEW_YORK_RANGERS(
			3, 
			"New York", 
			"Rangers", 
			"NYR", 
			Division.METRO, 
			"Lets Go Rangers!",
			Color.of(0x0038A8),
			ZoneId.of("America/New_York")),
	PHILADELPHIA_FLYERS(
			4, 
			"Philadelphia", 
			"Flyers", 
			"PHI", 
			Division.METRO, 
			"Lets Go Flyers!",
			Color.of(0xFA4616),
			ZoneId.of("America/New_York")),
	PITTSBURGH_PENGUINS(
			5, 
			"Pittsburgh", 
			"Penguins", 
			"PIT", 
			Division.METRO, 
			"Lets Go Pens!", 
			Color.of(0xFFB81C),
			ZoneId.of("America/New_York")),
	BOSTON_BRUINS(
			6, 
			"Boston", 
			"Bruins", 
			"BOS", 
			Division.ATLANTIC, 
			"Lets Go Bruins!", 
			Color.of(0xFFB81C),
			ZoneId.of("America/New_York")),
	BUFFALO_SABRES(
			7, 
			"Buffalo", 
			"Sabres", 
			"BUF", 
			Division.ATLANTIC, 
			"Lets Go Buffalo!", 
			Color.of(0xFFB81C),
			ZoneId.of("America/New_York")),
	MONTREAL_CANADIENS(
			8, 
			"Montréal", 
			"Canadiens", 
			"MTL", 
			Division.ATLANTIC, 
			"Olé Olé Olé",
			Color.of(0xA6192E),
			ZoneId.of("America/Montreal")),
	OTTAWA_SENATORS(
			9, 
			"Ottawa", 
			"Senators", 
			"OTT", 
			Division.ATLANTIC, 
			"Go Sens Go!", 
			Color.of(0xC8102E),
			ZoneId.of("America/Toronto")),
	TORONTO_MAPLE_LEAFS(
			10, 
			"Toronto", 
			"Maple Leafs", 
			"TOR", 
			Division.ATLANTIC, 
			"Go Leafs Go!",
			Color.of(0x00205B),
			ZoneId.of("America/Toronto")),
	CAROLINA_HURRICANES(
			12, 
			"Carolina", 
			"Hurricanes", 
			"CAR", 
			Division.METRO, 
			"Lets Go Canes!", 
			Color.of(0xCC0000),
			ZoneId.of("America/New_York")),
	FLORIDA_PANTHERS(
			13, 
			"Florida", 
			"Panthers", 
			"FLA", 
			Division.ATLANTIC, 
			"Lets Go Panthers!", 
			Color.of(0xB9975B),
			ZoneId.of("America/New_York")),
	TAMPA_BAY_LIGHTNING(
			14, 
			"Tampa Bay", 
			"Lightning", 
			"TBL", 
			Division.ATLANTIC, 
			"Lets Go Lightning!", 
			Color.of(0x00205B),
			ZoneId.of("America/New_York")),
	WASHINGTON_CAPITALS(
			15, 
			"Washington", 
			"Capitals", 
			"WSH", 
			Division.METRO, 
			"Lets Go Caps!", 
			Color.of(0xC8102E),
			ZoneId.of("America/New_York")),
	CHICAGO_BLACKHAWKS(
			16, 
			"Chicago", 
			"Blackhawks", 
			"CHI", 
			Division.CENTRAL, 
			"Lets Go Hawks!",
			Color.of(0xC8102E),
			ZoneId.of("America/Chicago")),
	DETROIT_RED_WINGS(
			17, 
			"Detroit", 
			"Red Wings",
			"DET",
			Division.ATLANTIC,
			"Lets Go Red Wings!",
			Color.of(0xC8102E),
			ZoneId.of("America/Detroit")),
	NASHVILLE_PREDATORS(
			18, 
			"Nashville", 
			"Predators", 
			"NSH", 
			Division.CENTRAL, 
			"Lets Go Predators!", 
			Color.of(0xFFB81C),
			ZoneId.of("America/Chicago")),
	ST_LOUIS_BLUES(
			19, 
			"St. Louis", 
			"Blues", 
			"STL", 
			Division.CENTRAL, 
			"Lets Go Blues!",
			Color.of(0x003087),
			ZoneId.of("America/Chicago")),
	CALGARY_FLAMES(
			20, 
			"Calgary", 
			"Flames", 
			"CGY", 
			Division.PACIFIC, 
			"Go Flames Go!", 
			Color.of(0xC8102E),
			ZoneId.of("America/Edmonton")),
	COLORADO_AVALANCH(
			21, 
			"Colorado", 
			"Avalanche", 
			"COL", 
			Division.CENTRAL, 
			"Lets Go Colorado!",
			Color.of(0x6F263D),
			ZoneId.of("America/Denver")),
	EDMONTON_OILERS(
			22, 
			"Edmonton",
			"Oilers",
			"EDM",
			Division.PACIFIC,
			"Let go Oilers!", 
			Color.of(0xFC4C02),
			ZoneId.of("America/Edmonton")),
	VANCOUVER_CANUCKS(
			23, 
			"Vancouver", 
			"Canucks", 
			"VAN", 
			Division.PACIFIC, 
			"Go Canucks Go!",
			Color.of(0x00843D),
			ZoneId.of("America/Vancouver")),
	ANAHEIM_DUCKS(
			24, 
			"Anaheim", 
			"Ducks", 
			"ANA", 
			Division.PACIFIC, 
			"Lets Go Ducks!",
			Color.of(0xFC4C02),
			ZoneId.of("America/Los_Angeles")),
	DALLAS_STARS(
			25, 
			"Dallas",
			"Stars",
			"DAL",
			Division.CENTRAL, 
			"Go Stars Go!", 
			Color.of(0x006341),
			ZoneId.of("America/Chicago")),
	LA_KINGS(
			26, 
			"Los Angeles",
			"Kings", 
			"LAK",
			Division.PACIFIC,
			"Go Kings Go!", 
			Color.of(0x000000),
			ZoneId.of("America/Los_Angeles")),
	SAN_JOSE_SHARKS(
			28, 
			"San Jose", 
			"Sharks",
			"SJS", 
			Division.PACIFIC, 
			"Lets Go Sharks!",
			Color.of(0x006272),
			ZoneId.of("America/Los_Angeles")),
	COLUMBUS_BLUE_JACKETS(
			29, 
			"Columbus", 
			"Blue Jackets", 
			"CBJ", 
			Division.METRO, 
			"C B J! C B J!", // WTF Seriously?
			Color.of(0x041E42),
			ZoneId.of("America/Chicago")),
	MINNESOTA_WILD(
			30, 
			"Minnesota",
			"Wild", 
			"MIN", 
			Division.CENTRAL, 
			"Lets Go Wild!", 
			Color.of(0x154734),
			ZoneId.of("America/Chicago")),
	WINNIPEG_JETS(
			52, 
			"Winnipeg", 
			"Jets", 
			"WPG", 
			Division.CENTRAL, 
			"Go Jets Go!", 
			Color.of(0xFFFFFF),
			ZoneId.of("America/Winnipeg")),
	ARIZONA_COYOTES(
			53, 
			"Arizona", 
			"Coyotes", 
			"ARI", 
			Division.PACIFIC, 
			"Lets Go Coyotes!", 
			Color.of(0x8C2633),
			ZoneId.of("America/Denver")),
	VEGAS_GOLDEN_KNIGHTS(
			54, 
			"Vegas", 
			"Golden Knights", 
			"VGK", 
			Division.PACIFIC, 
			"Go Knights Go!",
			Color.of(0xB9975B),
			ZoneId.of("America/Los_Angeles"));

	public static final String MULTI_TEAM_CHEER = "Lets Go!";

	private final int id;
	private final String location;
	private final String name;
	private final String code;
	private final Division division;
	private final String cheer;
	private final Color color;
	private final ZoneId timeZone;

	private static final Map<Integer, Team> VALUES_MAP = new HashMap<>();
	private static final Map<String, Team> CODES_MAP = new HashMap<>();
	private static final List<Team> SORTED_VALUES;

	static {
		for (Team t : Team.values()) {
			VALUES_MAP.put(t.id, t);
		}
		for (Team t : Team.values()) {
			CODES_MAP.put(t.code, t);
		}

		SORTED_VALUES = new ArrayList<>(EnumSet.allOf(Team.class));
		SORTED_VALUES.sort((t1, t2) -> t1.getFullName().compareTo(t2.getFullName()));
	}

	private Team(int id, String location, String name, String code, Division division, String cheer, Color color,
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

	public Color getColor() {
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

	public static List<Team> getSortedLValues() {
		return new ArrayList<>(SORTED_VALUES);
	}
}
