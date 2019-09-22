package com.hazeluff.discord.nhlbot;

import java.time.ZoneId;
import java.util.Properties;

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
	
	/**
	 * The starting year of season (e.g. 2017 for the 2017-2018 season)
	 */
	public static final int SEASON_YEAR = 2019;
	
	public static final String GIT_URL = "http://nhlbot.hazeluff.com";
	public static final String DONATION_URL = "https://paypal.me/hazeluff";
	public static final String DONATION_BTC = "1AzGixATkgDSzjPHaobXWL7dWVmzBqz9JD";
	public static final String DONATION_ETH = "0x313faE0D36BFf3F7a4817E52a71B74e2924D4b97";
	public static final String STATUS_MESSAGE = "?nhlbot help";
	public static final long HAZELUFF_ID = 225742618422673409l;
	public static final String HAZELUFF_MENTION = "@Hazeluff#0201";
	public static final String HAZELUFF_SITE = "http://www.hazeluff.com";
	public static final String HAZELUFF_EMAIL = "eugene@hazeluff.com";
	public static final String VERSION = "${project.version}";
	public static final String MONGO_HOST = "localhost";
	public static final int MONGO_PORT = 27017;
	public static final String MONGO_DATABASE_NAME = "NHLBot";
	public static final String MONGO_TEST_DATABASE_NAME = "NHLBotIntegrationTest";
	public static final ZoneId DATE_START_TIME_ZONE = ZoneId.of("America/Vancouver");

	public static final int HTTP_REQUEST_RETRIES = 5;
	public static final String NHL_API_URL = "https://statsapi.web.nhl.com/api/v1";	
	
	private static final Properties systemProperties = System.getProperties();
}
