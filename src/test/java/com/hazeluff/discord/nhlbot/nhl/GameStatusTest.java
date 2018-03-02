package com.hazeluff.discord.nhlbot.nhl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PowerMockRunner.class)
public class GameStatusTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(GameStatus.class);

	@Test
	public void parseShouldReturnGameStatus() {
		LOGGER.info("parseShouldReturnGameStatus");
		assertEquals(GameStatus.PREVIEW, GameStatus.parse(1));
		assertEquals(GameStatus.STARTED, GameStatus.parse(2));
		assertEquals(GameStatus.LIVE, GameStatus.parse(3));
		assertEquals(GameStatus.STARTED, GameStatus.parse(4));
		assertEquals(GameStatus.FINAL, GameStatus.parse(5));
		assertEquals(GameStatus.FINAL, GameStatus.parse(6));
		assertEquals(GameStatus.FINAL, GameStatus.parse(7));
	}

	@Test(expected = IllegalArgumentException.class)
	public void parseShouldThrowIllegalArgumentExceptionWhenIdIsUnknown() {
		LOGGER.info("parseShouldThrowIllegalArgumentExceptionWhenIdIsUnknown");
		GameStatus.parse(-1);
	}
}
