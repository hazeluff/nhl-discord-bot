package com.hazeluff.discord.nhlbot.bot.preferences;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.Config;
import com.hazeluff.discord.nhlbot.nhl.Team;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

/*
 * Note: We are not using @RunWith(PowerMockRunner.class) as it causes an ExceptionInInitializationError with
 * MongoClient. DiscordClient will not be mocked and will be null. Methods in GuildsPreferencesManager should not both
 * user DiscordClient and MongoDatabase, so that we can test them.
 */
public class GuildPreferencesManagerIT {
	private static final Logger LOGGER = LoggerFactory.getLogger(GuildPreferencesManagerIT.class);

	private static final String GUILD_ID = RandomStringUtils.randomNumeric(10);
	private static final Team TEAM = Team.VANCOUVER_CANUCKS;

	static MongoClient mongoClient;
	MongoDatabase mongoDatabase;

	GuildPreferencesManager guildPreferencesManager;

	@BeforeClass
	public static void beforeClass() {
		mongoClient = new MongoClient(Config.MONGO_HOST, Config.MONGO_PORT);
	}

	@AfterClass
	public static void afterClass() {
		if (mongoClient != null) {
			mongoClient.close();
		}
	}

	@Before
	public void before() {
		mongoDatabase = mongoClient.getDatabase(Config.MONGO_TEST_DATABASE_NAME);
		guildPreferencesManager = new GuildPreferencesManager(null, mongoDatabase);
	}

	@After
	public void after() {
		mongoDatabase.drop();
	}
	
	@Test
	public void subscribeShouldWriteToDatabase() {
		LOGGER.info("subscribeShouldWriteToDatabase");
		guildPreferencesManager.subscribe(GUILD_ID, TEAM);

		guildPreferencesManager = new GuildPreferencesManager(null, mongoDatabase);
		assertFalse(guildPreferencesManager.getPreferences().containsKey(GUILD_ID));
		guildPreferencesManager.loadPreferences();

		assertEquals(TEAM, guildPreferencesManager.getPreferences().get(GUILD_ID).getTeam());
	}

}