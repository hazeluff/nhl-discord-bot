package com.hazeluff.discord.nhlbot.nhl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PowerMockRunner.class)
public class GameEventStrengthTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(GameEventStrengthTest.class);

	@Test
	public void parseShouldReturnGameEventStrength() {
		LOGGER.info("parseShouldReturnGameEventStrength");
		for (GameEventStrength ges : GameEventStrength.values()) {
			assertEquals(ges, GameEventStrength.parse(ges.getId()));
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void parseShouldThrowIllegalArgumentExceptionWhenIdIsUnknown() {
		LOGGER.info("parseShouldThrowIllegalArgumentExceptionWhenIdIsUnknown");
		GameEventStrength.parse("?");
	}
}
