package utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.ZonedDateTime;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.utils.DateUtils;

@RunWith(PowerMockRunner.class)
public class DateUtilsTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(DateUtilsTest.class);
	
	@Test
	public void parseNHLDateShouldReturnDate() {
		LOGGER.info("parseNHLDateShouldReturnDate");
		ZonedDateTime result = DateUtils.parseNHLDate("2000-12-08T15:20:30Z");

		assertEquals(2000, result.getYear());
		assertEquals(12, result.getMonthValue());
		assertEquals(8, result.getDayOfMonth());
		assertEquals(15, result.getHour());
		assertEquals(20, result.getMinute());
		assertEquals(30, result.getSecond());
	}

	@Test
	public void parseNHLDateShouldReturnNullWhenDateIsInvalid() {
		LOGGER.info("parseNHLDateShouldReturnNullWhenDateIsInvalid");
		ZonedDateTime result = DateUtils.parseNHLDate("asdf");

		assertNull(result);
	}

}
