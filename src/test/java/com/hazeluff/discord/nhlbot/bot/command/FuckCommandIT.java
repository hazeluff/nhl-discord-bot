package com.hazeluff.discord.nhlbot.bot.command;

import static org.junit.Assert.assertTrue;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.test.DatabaseIT;
import com.mongodb.MongoClient;


public class FuckCommandIT extends DatabaseIT {
	private static final Logger LOGGER = LoggerFactory.getLogger(FuckCommandIT.class);

	private FuckCommand fuckCommand;

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
		fuckCommand = new FuckCommand(getNHLBot());
	}
	
	@Test
	public void addShouldWriteToDatabase() {
		LOGGER.info("addShouldWriteToDatabase");
		String subject = RandomStringUtils.randomAlphanumeric(4);
		String response1 = RandomStringUtils.randomAlphanumeric(6);
		String response2 = RandomStringUtils.randomAlphanumeric(6);
		fuckCommand.add(subject, response1);
		fuckCommand.add(subject, response2);

		fuckCommand = new FuckCommand(getNHLBot());

		assertTrue(fuckCommand.getResponses(subject).contains(response1));
		assertTrue(fuckCommand.getResponses(subject.toUpperCase()).contains(response1));
		assertTrue(fuckCommand.getResponses(subject.toLowerCase()).contains(response1));
		assertTrue(fuckCommand.getResponses(subject).contains(response2));
		assertTrue(fuckCommand.getResponses(subject.toUpperCase()).contains(response2));
		assertTrue(fuckCommand.getResponses(subject.toLowerCase()).contains(response2));
	}
}