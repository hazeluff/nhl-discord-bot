package com.hazeluff.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;

import com.hazeluff.discord.nhlbot.Config;
import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.bot.discord.DiscordManager;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

/*
 * Note: We are not using @RunWith(PowerMockRunner.class) as it causes an ExceptionInInitializationError with
 * MongoClient. DiscordClient will not be mocked and will be null. Methods in GuildsPreferencesManager should not both
 * use DiscordClient and MongoDatabase, so that we can test them.
 */
public class DatabaseIT {
	private static final MongoClient mongoClient;

	private MongoDatabase mongoDatabase;
	private NHLBot nhlBot;

	static {
		mongoClient = new MongoClient(Config.MONGO_HOST, Config.MONGO_PORT);
	}

	@Before
	public void before() {
		mongoDatabase = mongoClient.getDatabase(Config.MONGO_TEST_DATABASE_NAME);
		nhlBot = mock(NHLBot.class);
		when(nhlBot.getMongoDatabase()).thenReturn(mongoDatabase);
		when(nhlBot.getDiscordManager()).thenReturn(mock(DiscordManager.class));
	}

	@AfterClass
	public static void afterClass() {
		if (mongoClient != null) {
			mongoClient.close();
		}
	}

	@After
	public void after() {
		mongoDatabase.drop();
	}

	protected MongoDatabase getDatabase() {
		return mongoDatabase;
	}

	protected NHLBot getNHLBot() {
		return nhlBot;
	}
}
