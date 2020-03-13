package com.hazeluff.discord.nhlbot.nhl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.nhl.GamePeriod.Type;

@RunWith(PowerMockRunner.class)
public class GamePeriodTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(GamePeriodTest.class);

	@Test
	public void typeParseShouldReturnGamePeriod() {
		LOGGER.info("parseShouldReturnGamePeriod");
		for (Type t : Type.values()) {
			assertEquals(t, Type.parse(t.getId()));
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void typeParseShouldThrowIllegalArgumentExceptionWhenIdIsUnknown() {
		LOGGER.info("parseShouldThrowIllegalArgumentExceptionWhenIdIsUnknown");
		Type.parse("?");
	}

	@Test
	public void getDisplayValueShouldReturnString() {
		assertEquals("1st Period", new GamePeriod(1, Type.REGULAR, "1st").getDisplayValue());
		assertEquals("3rd Overtime", new GamePeriod(6, Type.OVERTIME, "3rd").getDisplayValue());
		assertEquals("Shootout", new GamePeriod(7, Type.SHOOTOUT, "").getDisplayValue());
	}
}
