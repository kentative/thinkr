package com.bytes.fmk.service.leaderboard;

/**
 * Leaderboard reward cycle duration.
 *  
 */
public enum Cycle {
	
	/**
	 * The current day, starting at 00:00 local time until
	 * the next day (24 hours) later.
	 * This is 00:00 to 24:00
	 */
	Daily,
	
	/**
	 * The first day of the current week, at 7:00AM until midnight of the last day 
	 * of the week. This is 00:00 Monday until 24:00 Sunday.
	 */
	Weekly,
	
	/**
	 * First day the current month until the last day.
	 * This is 00:00 the 1st until 24:00 the last day of the month.  
	 * For a fixed 30-day cycle, user {@code Custom}
	 */
	Monthly,
	
	/**
	 * Defaults to 1 year, starting at the time of creation.
	 * Can accommodate any start time to end time.
	 */
	Custom
}