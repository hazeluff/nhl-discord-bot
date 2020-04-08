package com.hazeluff.discord.canucks.bot.database.pole;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.test.DatabaseIT;
import com.mongodb.MongoClient;

public class PolesManagerIT extends DatabaseIT {
	private static final Logger LOGGER = LoggerFactory.getLogger(PolesManagerIT.class);

	PollsManager polesManager;

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
		polesManager = new PollsManager(getDatabase());
	}

	@Test
	public void polesCanBeSavedAndLoaded() {
		LOGGER.info("polesCanBeSavedAndLoaded");
		long channelId = 100;
		long messageId = 200;
		String poleId = "test";
		Map<String, String> reactions = new HashMap<>();
		reactions.put("react1", "option1");
		reactions.put("react1", "option1");
		PollMessage message = new PollMessage(channelId, messageId, poleId, reactions);
		
		assertNull(polesManager.loadPoll(channelId, poleId));
		polesManager.savePoll(message);
		assertEquals(message, polesManager.loadPoll(channelId, poleId));
		assertEquals(message, new PollsManager(getDatabase()).loadPoll(channelId, poleId));
	}
}