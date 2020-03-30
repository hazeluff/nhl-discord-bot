package com.hazeluff.discord.canucks.bot.database.pole;

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

	PolesManager polesManager;

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
		polesManager = new PolesManager(getDatabase());
	}

	@Test
	public void polesCanBeSavedAndLoaded() {
		LOGGER.info("polesCanBeSavedAndLoaded");

	}
}