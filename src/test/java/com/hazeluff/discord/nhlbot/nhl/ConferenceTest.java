package com.hazeluff.discord.nhlbot.nhl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PowerMockRunner.class)
public class ConferenceTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConferenceTest.class);

	@Test
	public void parseShouldReturnConference() {
		LOGGER.info("parseShouldReturnConference");
		for (Conference c : Conference.values()) {
			assertEquals(c, Conference.parse(c.getId()));
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void parseShouldThrowIllegalArgumentExceptionWhenIdIsUnknown() {
		LOGGER.info("parseShouldThrowIllegalArgumentExceptionWhenIdIsUnknown");
		Conference.parse(-1);
	}
}
