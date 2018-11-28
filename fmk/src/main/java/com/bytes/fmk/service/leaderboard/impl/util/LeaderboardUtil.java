package com.bytes.fmk.service.leaderboard.impl.util;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bytes.fmk.service.leaderboard.Cycle;

public class LeaderboardUtil {

	private static String DEFAULT_ZONE_OFFSET_ID = "-7";
	
	private static Logger logger = LoggerFactory.getLogger(LeaderboardUtil.class);
	
	/**
	 * Set the start and end time based on this:
	 * start = today but at 00:00
	 * end   = today + cycle duration 00:00
	 * @param atTime - the active time of this leaderboard 
	 */
	public static OffsetDateTime getStartTime(Cycle cycle, OffsetDateTime atTime) {
		
		logger.debug("atTime before: " + atTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
		OffsetDateTime time = atTime.withNano(0).withSecond(0).withMinute(0)
				.withOffsetSameInstant(ZoneOffset.of(DEFAULT_ZONE_OFFSET_ID));
		logger.debug("atTime after:  " + time.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
		
		switch (cycle) {
		
		case Daily:
			return time.withHour(0);
		
		case Weekly:
			int dayOffset = time.getDayOfWeek().getValue()%7;
			return time.minusDays(dayOffset).withHour(0);
			
		case Monthly:
			return time.withHour(0).withDayOfMonth(1);

		default:
			// Default to 1 year, expects date to be set manually
			// Defaults start time to beginning of today
			return time.withHour(0);
		}
	}

}
