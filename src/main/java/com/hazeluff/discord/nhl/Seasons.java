package com.hazeluff.discord.nhl;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class Seasons {
	public static final Season S20_21 = new Season(
			ZonedDateTime.of(2021, 1, 12, 0, 0, 0, 0, ZoneOffset.UTC),
			ZonedDateTime.of(2021, 12, 31, 0, 0, 0, 0, ZoneOffset.UTC),
			2020,			
			2021,
			"20-21");

	public static class Season {
		private final ZonedDateTime startDate;
		private final ZonedDateTime endDate;
		private final int startYear;
		private final int endYear;
		private final String abbreviation;

		public Season(ZonedDateTime startDate, ZonedDateTime endDate, int startYear, int endYear, String abbreviation) {
			this.startDate = startDate;
			this.endDate = endDate;
			this.startYear = startYear;
			this.endYear = endYear;
			this.abbreviation = abbreviation;
		}

		public ZonedDateTime getStartDate() {
			return startDate;
		}

		public ZonedDateTime getEndDate() {
			return endDate;
		}

		public int getStartYear() {
			return startYear;
		}

		public int getEndYear() {
			return endYear;
		}

		public String getAbbreviation() {
			return abbreviation;
		}
	}

}
