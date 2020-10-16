package com.hazeluff.discord.bot.database.preferences;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhl.Team;
import com.hazeluff.discord.utils.Utils;
import com.hazeluff.test.DatabaseIT;
import com.mongodb.MongoClient;

public class PreferencesDataIT extends DatabaseIT {
	private static final Logger LOGGER = LoggerFactory.getLogger(PreferencesDataIT.class);

	private static final long GUILD_ID = Utils.getRandomLong();
	private static final Team TEAM = Team.VANCOUVER_CANUCKS;
	private static final Team TEAM2 = Team.CALGARY_FLAMES;

	PreferencesData preferencesManager;

	private static MongoClient client;

	@Override
	public MongoClient getClient() {
		return client;
	}

	@BeforeClass
	public static void setupConnection() {
		client = createConnection();
	}

	@AfterClass
	public static void closeConnection() {
		closeConnection(client);
	}

	@Before
	public void before() {
		super.before();
		preferencesManager = PreferencesData.load(getDatabase());
	}
	
	@Test
	public void subscribeGuildShouldWriteToDatabase() {
		LOGGER.info("subscribeGuildShouldWriteToDatabase");
		preferencesManager.subscribeGuild(GUILD_ID, TEAM);
		preferencesManager.subscribeGuild(GUILD_ID, TEAM2);

		// Reload
		preferencesManager = PreferencesData.load(getDatabase());
		assertTrue(Utils.isListEquivalent(Arrays.asList(TEAM, TEAM2),
				preferencesManager.getGuildPreferences(GUILD_ID).getTeams()));
	}

	@Test
	public void unsubscribeGuildShouldWriteToDatabase() {
		LOGGER.info("unsubscribeGuildShouldWriteToDatabase");
		preferencesManager.subscribeGuild(GUILD_ID, TEAM);
		preferencesManager.subscribeGuild(GUILD_ID, TEAM2);
		preferencesManager.unsubscribeGuild(GUILD_ID, TEAM);

		// Reload
		preferencesManager = PreferencesData.load(getDatabase());
		assertTrue(Utils.isListEquivalent(
				Arrays.asList(TEAM2),
				preferencesManager.getGuildPreferences(GUILD_ID).getTeams()));
	}

}