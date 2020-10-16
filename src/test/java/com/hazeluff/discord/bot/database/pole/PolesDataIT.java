package com.hazeluff.discord.bot.database.pole;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.test.DatabaseIT;
import com.mongodb.MongoClient;

public class PolesDataIT extends DatabaseIT {
	private static final Logger LOGGER = LoggerFactory.getLogger(PolesDataIT.class);

	PollsData polesManager;

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
		polesManager = new PollsData(getDatabase());
	}

	@Test
	public void polesCanBeSavedAndLoaded() {
		LOGGER.info("polesCanBeSavedAndLoaded");
		long channelId = 100;
		long messageId = 200;
		String poleId = "test";
		PollMessage message = new PollMessage(channelId, messageId, poleId);
		
		assertNull(polesManager.loadPoll(channelId, poleId));
		polesManager.savePoll(message);
		assertEquals(message, polesManager.loadPoll(channelId, poleId));
		assertEquals(message, new PollsData(getDatabase()).loadPoll(channelId, poleId));
	}
}