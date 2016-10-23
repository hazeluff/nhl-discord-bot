package com.hazeluff.discort.canucksbot.utils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class DateUtils {
	private static final SimpleDateFormat DEBUG_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");

	public static String toString(Date date) {
		return date == null ? "" : DEBUG_FORMAT.format(date);
	}

	public static int compareNoTime(Date date1, Date date2) {
		LocalDate d1 = date1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate d2 = date2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		return d1.compareTo(d2);
	}

	public static long diff(Date d1, Date d2) {
		return d1.getTime() - d2.getTime();
	}
}
