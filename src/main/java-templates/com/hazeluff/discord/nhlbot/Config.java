package com.hazeluff.discord.nhlbot;

import java.util.Properties;

public class Config {
	public static final String GIT_URL = "http://nhlbot.hazeluff.com";
	public static final String STATUS_MESSAGE = "@NHLBot help";
	public static final String HAZELUFF_ID = "225742618422673409";
	public static final String HAZELUFF_MENTION = "<@" + HAZELUFF_ID + ">";
	public static final String HAZELUFF_SITE = "http://www.hazeluff.com";
	public static final String HAZELUFF_EMAIL = "me@hazeluff.com";
	public static final String VERSION = "${project.version}";
	public static final String MONGO_HOST = "localhost";
	public static final int MONGO_PORT = 27017;
	public static final String MONGO_DATABASE_NAME = "NHLBot";
	public static final String MONGO_TEST_DATABASE_NAME = "NHLBotIntegrationTest";

	public static final int HTTP_REQUEST_RETRIES = 5;
	public static final String NHL_API_URL = "https://statsapi.web.nhl.com/api/v1";
	
	
	private static final Properties systemProperties = System.getProperties();
	
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
