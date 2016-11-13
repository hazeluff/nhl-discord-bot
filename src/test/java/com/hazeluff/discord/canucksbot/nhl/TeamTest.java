package com.hazeluff.discord.canucksbot.nhl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PowerMockRunner.class)
public class TeamTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(TeamTest.class);

	@Test
	public void parseShouldReturnTeam() {
		LOGGER.info("parseShouldReturnTeam");
		for (Team t : Team.values()) {
			assertEquals(t, Team.parse(t.getId()));
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void parseShouldThrowIllegalArgumentExceptionWhenIdIsUnknown() {
		LOGGER.info("parseShouldThrowIllegalArgumentExceptionWhenIdIsUnknown");
		Team.parse(-1);
	}
}
