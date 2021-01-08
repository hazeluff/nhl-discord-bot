package com.hazeluff.discord;

import java.time.ZoneId;
import java.util.Properties;

import com.hazeluff.discord.nhl.Seasons;
import com.hazeluff.discord.nhl.Seasons.Season;

public class Config {
	public static class Debug {
		private static final String LOAD_GAMES_KEY = "load.games";

		public static boolean isLoadGames() {
			boolean hasKey = systemProperties.containsKey(LOAD_GAMES_KEY);
			if(!hasKey) {
				return true;
			}
			String strValue = systemProperties.getProperty(LOAD_GAMES_KEY);
			return strValue.isEmpty() || Boolean.valueOf(strValue);
		}
	}

	public static final Season CURRENT_SEASON = Seasons.S20_21;

	public static final String GIT_URL = "http://canucks-discord.hazeluff.com/";
	public static final String DONATION_URL = "https://paypal.me/hazeluff";
	public static final String DONATION_BTC = "1AzGixATkgDSzjPHaobXWL7dWVmzBqz9JD";
	public static final String DONATION_ETH = "0x313faE0D36BFf3F7a4817E52a71B74e2924D4b97";
	public static final String STATUS_MESSAGE = "?canucksbot help";
	public static final long HAZELUFF_ID = 225742618422673409l;
	public static final String HAZELUFF_MENTION = "<@225742618422673409>";
	public static final String HAZELUFF_SITE = "http://www.hazeluff.com";
	public static final String HAZELUFF_EMAIL = "me@hazeluff.com";
	public static final String HAZELUFF_TWITTER = "@Hazeluff";
	public static final String VERSION = "${project.version}";
	public static final String MONGO_HOST = "localhost";
	public static final int MONGO_PORT = 27017;
	public static final String MONGO_DATABASE_NAME = "CanucksBot";
	public static final String MONGO_TEST_DATABASE_NAME = "CanucksBotIntegrationTest";
	public static final ZoneId DATE_START_TIME_ZONE = ZoneId.of("America/Vancouver");

	public static final int HTTP_REQUEST_RETRIES = 5;
	public static final String NHL_API_URL = "https://statsapi.web.nhl.com/api/v1";	

	private static final Properties systemProperties = System.getProperties();
}
