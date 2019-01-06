package com.hazeluff.discord.nhlbot.bot.command;

import static org.junit.Assert.assertEquals;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.test.DatabaseIT;


public class FuckCommandIT extends DatabaseIT {
	private static final Logger LOGGER = LoggerFactory.getLogger(FuckCommandIT.class);

	private FuckCommand fuckCommand;

	@Before
	public void before() {
		super.before();
		fuckCommand = new FuckCommand(getNHLBot());
	}
	
	@Test
	public void addShouldWriteToDatabase() {
		LOGGER.info("addShouldWriteToDatabase");
		String subject = RandomStringUtils.random(4);
		String response1 = RandomStringUtils.random(6);
		String response2 = RandomStringUtils.random(6);
		fuckCommand.add(subject, response1);
		fuckCommand.add(subject, response2);

		fuckCommand = new FuckCommand(getNHLBot());

		assertEquals(response1, fuckCommand.getResponses().get(subject));
		assertEquals(response2, fuckCommand.getResponses().get(subject));
	}
}