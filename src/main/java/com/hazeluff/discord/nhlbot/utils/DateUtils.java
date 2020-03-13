package com.hazeluff.discord.nhlbot.utils;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(DateUtils.class);
	private static final DateTimeFormatter NHL_API_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");

	public static ZonedDateTime parseNHLDate(String date) {
		try {
			return ZonedDateTime.parse(date.replaceAll("Z", "+0000"), NHL_API_FORMAT);
		} catch (DateTimeParseException e) {
			LOGGER.error("Could not parse date [" + date + "]", e);
			return null;
		}
	}

	/**
	 * Finds the difference (in ms) between two LocalDateTime. Returns date2 - date1.
	 * 
	 * @param date1
	 * @param date2
	 * @return time difference in ms
	 */
	public static long diffMs(ZonedDateTime date1, ZonedDateTime date2) {
		return Duration.between(date1, date2).getSeconds() * 1000;
	}

	/**
	 * Determines if a date is between a date range
	 * 
	 * @param date
	 * @param start
	 *            start of the range
	 * @param end
	 *            end of the range
	 * @return true if the date is between the range
	 */
	public static boolean isBetweenRange(ZonedDateTime date, ZonedDateTime start, ZonedDateTime end) {
		return diffMs(start, date) > 0 && diffMs(date, end) > 0;
	}

	/**
	 * Gets {@link ZonedDateTime#now()}. Used for stubbing in tests.
	 * 
	 * @return
	 */
	public static ZonedDateTime now() {
		return ZonedDateTime.now();
	}
}
