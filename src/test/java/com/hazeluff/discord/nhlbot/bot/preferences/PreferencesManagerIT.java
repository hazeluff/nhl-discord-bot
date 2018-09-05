package com.hazeluff.discord.nhlbot.bot.preferences;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.Document;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.Config;
import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.bot.discord.DiscordManager;
import com.hazeluff.discord.nhlbot.nhl.Team;
import com.hazeluff.discord.nhlbot.utils.Utils;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;

/*
 * Note: We are not using @RunWith(PowerMockRunner.class) as it causes an ExceptionInInitializationError with
 * MongoClient. DiscordClient will not be mocked and will be null. Methods in GuildsPreferencesManager should not both
 * use DiscordClient and MongoDatabase, so that we can test them.
 */
public class PreferencesManagerIT {
	private static final Logger LOGGER = LoggerFactory.getLogger(PreferencesManagerIT.class);

	private static final long GUILD_ID = Utils.getRandomLong();
	private static final Team TEAM = Team.VANCOUVER_CANUCKS;
	private static final Team TEAM2 = Team.CALGARY_FLAMES;

	static MongoClient mongoClient;
	MongoDatabase mongoDatabase;
	NHLBot nhlBot;

	PreferencesManager preferencesManager;

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
		nhlBot = mock(NHLBot.class);
		when(nhlBot.getMongoDatabase()).thenReturn(mongoDatabase);
		when(nhlBot.getDiscordManager()).thenReturn(mock(DiscordManager.class));
		preferencesManager = new PreferencesManager(nhlBot);
	}

	@After
	public void after() {
		mongoDatabase.drop();
	}
	
	@Test
	public void subscribeGuildShouldWriteToDatabase() {
		LOGGER.info("subscribeGuildShouldWriteToDatabase");
		preferencesManager.subscribeGuild(GUILD_ID, TEAM);
		preferencesManager.subscribeGuild(GUILD_ID, TEAM2);

		// Reload
		preferencesManager = new PreferencesManager(nhlBot);
		assertFalse(preferencesManager.getGuildPreferences().containsKey(GUILD_ID));
		preferencesManager.loadPreferences();

		assertTrue(Utils.isListEquivalent(Arrays.asList(TEAM, TEAM2),
				preferencesManager.getGuildPreferences().get(GUILD_ID).getTeams()));
	}

	@Test
	public void unsubscribeGuildShouldWriteToDatabase() {
		LOGGER.info("unsubscribeGuildShouldWriteToDatabase");
		preferencesManager.subscribeGuild(GUILD_ID, TEAM);
		preferencesManager.subscribeGuild(GUILD_ID, TEAM2);
		preferencesManager.unsubscribeGuild(GUILD_ID, TEAM);

		// Reload
		preferencesManager = new PreferencesManager(nhlBot);
		assertFalse(preferencesManager.getGuildPreferences().containsKey(GUILD_ID));
		preferencesManager.loadPreferences();

		assertTrue(Utils.isListEquivalent(
				Arrays.asList(TEAM2),
				preferencesManager.getGuildPreferences().get(GUILD_ID).getTeams()));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void loadPreferencesShouldLoadLegacyTeamData() {
		preferencesManager = new PreferencesManager(nhlBot);
		preferencesManager.getGuildCollection().updateOne(
				new Document("id", GUILD_ID),
				new Document("$set", new Document("team", TEAM.getId())), 
				new UpdateOptions().upsert(true));

		preferencesManager.loadPreferences();

		assertTrue(Utils.isListEquivalent(Arrays.asList(TEAM),
				preferencesManager.getGuildPreferences().get(GUILD_ID).getTeams()));

		Document doc = preferencesManager.getGuildCollection().find().iterator().next();
		
		assertEquals(Arrays.asList(TEAM),
				((List<Integer>) doc.get("teams")).stream().map(Team::parse).collect(Collectors.toList()));
	}

}