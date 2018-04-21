package com.hazeluff.discord.nhlbot.nhl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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

	@Test
	public void parseShouldReturnNullWhenIdIsNull() {
		LOGGER.info("parseShouldReturnNullWhenIdIsNull");
		assertNull(Team.parse((Integer) null));
	}

	@Test(expected = IllegalArgumentException.class)
	public void parseShouldThrowIllegalArgumentExceptionWhenIdIsUnknown() {
		LOGGER.info("parseShouldThrowIllegalArgumentExceptionWhenIdIsUnknown");
		Team.parse(-1);
	}

	@Test
	public void parseByCodeShouldReturnTeam() {
		LOGGER.info("parseByCodeShouldReturnTeam");

		for (Team t : Team.values()) {
			assertEquals(t, Team.parse(t.getCode().toLowerCase()));
		}
		
		for (Team t : Team.values()) {
			assertEquals(t, Team.parse(t.getCode().toUpperCase()));
		}
	}

	@Test
	public void parseByCodeShouldReturnNullWhenCodeIsNullOrEmpty() {
		LOGGER.info("parseByCodeShouldReturnNullWhenCodeIsNullOrEmpty");
		assertNull(Team.parse((String) null));
		assertNull(Team.parse(""));
	}

	@Test(expected = IllegalArgumentException.class)
	public void parseByCodeShouldThrowIllegalArgumentExceptionWhenCodeIsInvalid() {
		LOGGER.info("parseByCodeShouldThrowIllegalArgumentExceptionWhenCodeIsInvalid");
		Team.parse("asdf");
	}
}
