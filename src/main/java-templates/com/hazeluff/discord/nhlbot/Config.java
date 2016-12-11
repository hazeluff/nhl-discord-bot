package com.hazeluff.discord.nhlbot;

public class Config {
	public static final String GIT_URL = "http://nhlbot.hazeluff.com";
	public static final String HAZELUFF_ID = "225742618422673409";
	public static final String HAZELUFF_MENTION = "<@" + HAZELUFF_ID + ">";
	public static final String HAZELUFF_EMAIL = "me@hazeluff.com";
	public static final String VERSION = "${project.version}";

	public static final int HTTP_REQUEST_RETRIES = 5;
	public static final String NHL_API_URL = "https://statsapi.web.nhl.com/api/v1";
}
