package com.hazeluff.discord.nhlbot.nhl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PowerMockRunner.class)
public class GameEventTypeTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(GameEventTypeTest.class);

	@Test
	public void parseShouldReturnGameEventType() {
		LOGGER.info("parseShouldReturnGameEventType");
		for (GameEventType get : GameEventType.values()) {
			assertEquals(get, GameEventType.parse(get.getId()));
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void parseShouldThrowIllegalArgumentExceptionWhenIdIsUnknown() {
		LOGGER.info("parseShouldThrowIllegalArgumentExceptionWhenIdIsUnknown");
		GameEventType.parse("?");
	}
}
