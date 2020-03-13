package com.hazeluff.discord.nhlbot.nhl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PowerMockRunner.class)
public class DivisionTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(DivisionTest.class);

	@Test
	public void parseShouldReturnDivision() {
		LOGGER.info("parseShouldReturnDivision");
		for (Division d : Division.values()) {
			assertEquals(d, Division.parse(d.getId()));
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void parseShouldThrowIllegalArgumentExceptionWhenIdIsUnknown() {
		LOGGER.info("parseShouldThrowIllegalArgumentExceptionWhenIdIsUnknown");
		Division.parse(-1);
	}
}
