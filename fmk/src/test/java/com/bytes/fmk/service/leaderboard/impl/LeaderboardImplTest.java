package com.bytes.fmk.service.leaderboard.impl;

import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.bytes.fmk.service.leaderboard.Cycle;
import com.bytes.fmk.service.leaderboard.Leaderboard;

public class LeaderboardImplTest {

	private OffsetDateTime defaultTime;
	
	
	
	@Before
	public void setup() {
		defaultTime = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
	}
	
	
	/**
	 * The current day, starting at 00:00 local time until
	 * the next day (24 hours) later.
	 * This is 00:00 to 24:00
	 */
	@Test
	public void daily() {
		Leaderboard leaderboard = new LeaderboardImpl("Test", Cycle.Daily);
		Assert.assertTrue(Cycle.Daily + " is leaderboard is active", leaderboard.isActive(defaultTime));

		OffsetDateTime startDate = leaderboard.getStartDate();
		OffsetDateTime endDate = leaderboard.getEndDate();
		
		Assert.assertEquals("Daily leaderboard duration", 23, startDate.until(endDate, ChronoUnit.HOURS));
		Assert.assertTrue("Daily leaderboard is active", leaderboard.isActive(defaultTime));
		OffsetDateTime time = OffsetDateTime.now().withNano(0).withSecond(0).withMinute(0).withHour(0);
		Assert.assertEquals("Start time HourOfDay", time, startDate);
		
		final OffsetDateTime now = endDate.plusMinutes(1);
		Assert.assertFalse("Daily is not active", leaderboard.isActive(now));
	}
	
	
	/**
	 * First day the current month until the last day.
	 * This is 00:00 the 1st until 24:00 the last day of the month.  
	 * For a fixed 30-day cycle, user {@code Custom}
	 */
	@Test
	public void weekly() {
		Leaderboard leaderboard = new LeaderboardImpl("Test", Cycle.Weekly);
		Assert.assertTrue(Cycle.Weekly + " is leaderboard is active", leaderboard.isActive(defaultTime));

		OffsetDateTime startDate = leaderboard.getStartDate();
		OffsetDateTime endDate = leaderboard.getEndDate();
		
		Assert.assertEquals("Weekly leaderboard duration", (24*7)-1, startDate.until(endDate, ChronoUnit.HOURS));
		Assert.assertTrue("Weekly leaderboard active", leaderboard.isActive(defaultTime));
		Assert.assertEquals("Start time DayOfWeek", DayOfWeek.SUNDAY, startDate.getDayOfWeek());
		Assert.assertEquals("Start time HourOfDay", 0, startDate.getHour());
		
		final OffsetDateTime now = endDate.plusMinutes(1);
		Assert.assertFalse("Leaderboard is not active", leaderboard.isActive(now));
	}
	
	
	/**
	 * First day the current month until the last day.
	 * This is 00:00 the 1st until 24:00 the last day of the month.  
	 * For a fixed 30-day cycle, user {@code Custom}
	 */
	@Test
	public void monthly() {
		Leaderboard leaderboard = new LeaderboardImpl("Test", Cycle.Monthly);
		Assert.assertTrue(Cycle.Monthly + " is leaderboard is active", leaderboard.isActive(defaultTime));

		OffsetDateTime startDate = leaderboard.getStartDate();
		OffsetDateTime endDate = leaderboard.getEndDate();
		
		// The first day of the month until the last day
		OffsetDateTime startMonth = OffsetDateTime.now().withDayOfMonth(1);
		OffsetDateTime endMonth = startMonth.plusMonths(1);
		
		int daysInMonth = (int) startMonth.until(endMonth, ChronoUnit.DAYS);
		
		Assert.assertEquals("Monthly leaderboard duration", (daysInMonth*24)-1, startDate.until(endDate, ChronoUnit.HOURS));
		Assert.assertTrue("Monthly leaderboard is active", leaderboard.isActive(defaultTime));
		Assert.assertEquals("Start time DayOfWeek", 1, startDate.getDayOfMonth());
		Assert.assertEquals("Start time HourOfDay", 0, startDate.getHour());
		
		final OffsetDateTime now = endDate.plusMinutes(1);
		Assert.assertFalse("Monthly leaderboard is not active", leaderboard.isActive(now));
	}
	
	
	/**
	 * Defaults to 1 year, starting at the time of creation.
	 * Can accommodate any start time to end time.
	 */
	@Test
	public void custom() {
		
		Leaderboard leaderboard = new LeaderboardImpl("Test", Cycle.Custom);
		OffsetDateTime start = OffsetDateTime.now().minusMinutes(1);
		leaderboard.setStartDate(start);
		leaderboard.setEndDate(start.plusDays(1));
		
		Assert.assertTrue(Cycle.Custom + " is leaderboard is active", leaderboard.isActive(defaultTime));

		OffsetDateTime startDate = leaderboard.getStartDate();
		OffsetDateTime endDate = leaderboard.getEndDate();
		
		Assert.assertEquals(24, startDate.until(endDate, ChronoUnit.HOURS));
		
		leaderboard.setStartDate(OffsetDateTime.now().minusHours(1));
		Assert.assertTrue("Custom leaderboard is active", leaderboard.isActive(defaultTime));
		Assert.assertEquals("Start time", start, startDate);
		
		final OffsetDateTime now = endDate.plusMinutes(1);
		Assert.assertFalse("Leaderboard is not active", leaderboard.isActive(now));
	}
}
