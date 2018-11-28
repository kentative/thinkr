package com.bytes.fmk.service.leaderboard;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.Test;

public class AutoRenewalTest extends LeaderboardServiceTestBase {

	private String title = "AutoRenew";
	
	private String category = "Karma";

	
	/**
	 * Create a leaderboard set.
	 * Set the autoRenew flag
	 * Update the score pass its active time
	 * Validate a new leaderboard is created and properly updated
	 * Validate the expired leaderboard is preserved
	 */
	@Test
	public void autoRenewDaily() {
		
		Cycle cycle = Cycle.Daily;
		
		// Create a leaderboard set, and set autoRenew
		LeaderboardSet set = service.createSet(title, cycle);
		service.getLeaderboard(set.getLeaderboard(cycle)).setAutoRenew(true);
		
		// Add users
		service.addUser(set.getId(), user01);
		service.addUser(set.getId(), user02);
		service.addUser(set.getId(), user03);

		ZoneOffset localOffset = OffsetDateTime.now().getOffset();
		for (int day = 1; day <= 3; day++) {
			for (int hour = 0; hour < 24; hour++) {
				OffsetDateTime time = OffsetDateTime.of(2018, 06, day, hour, 0, 0, 0, localOffset);
				service.update(set.getId(), user01.getId(), new Scorable(category, 1, time.plusMinutes(1)));
			}
		}

		// Assert historical leaderboards
		Leaderboard day1 = service.getLeaderboard(set.getId(), cycle, OffsetDateTime.of(2018, 06, 1, 0, 0, 0, 0, localOffset));
		LeaderboardTestUtil.assertScoreboards(day1);
		LeaderboardTestUtil.assertScore(day1, user01.getId(), 24);
		
		Leaderboard day2 = service.getLeaderboard(title + "_" + cycle + "_" + "06-02-2018");
		LeaderboardTestUtil.assertScoreboards(day2);
		LeaderboardTestUtil.assertScore(day2, user01.getId(), 24);
		
		Leaderboard day3 = service.getLeaderboard(title + "_" + cycle + "_" + "06-03-2018");
		LeaderboardTestUtil.assertScoreboards(day3);
		LeaderboardTestUtil.assertScore(day3, user01.getId(), 24);
	}
	
	
	@Test
	public void autoRenewWeekly() {
		
		Cycle cycle = Cycle.Weekly;
		
		// Create a leaderboard set, and set autoRenew
		LeaderboardSet set = service.createSet(title, cycle);
		service.getLeaderboard(set.getLeaderboard(cycle)).setAutoRenew(true);
		
		// Add users
		service.addUser(set.getId(), user01);
		service.addUser(set.getId(), user02);
		service.addUser(set.getId(), user03);

		ZoneOffset localOffset = OffsetDateTime.now().getOffset();
		for (int day = 1; day <= 21; day+=7) {
			for (int hour = 0; hour < 24; hour++) {
				OffsetDateTime time = OffsetDateTime.of(2018, 06, day, hour, 0, 0, 0, localOffset);
				service.update(set.getId(), user01.getId(), new Scorable(category, 2, time.plusMinutes(1)));
			}
		}

		// Assert historical leaderboards
		
		Leaderboard week1 = service.getLeaderboard(set.getId(), cycle, OffsetDateTime.of(2018, 06, 1, 0, 0, 0, 0, localOffset));
		LeaderboardTestUtil.assertScoreboards(week1);
		LeaderboardTestUtil.assertScore(week1, user01.getId(), 48);
		
		Leaderboard week2 = service.getLeaderboard(set.getId(), cycle, OffsetDateTime.of(2018, 06, 7, 0, 0, 0, 0, localOffset));
		LeaderboardTestUtil.assertScoreboards(week2);
		LeaderboardTestUtil.assertScore(week2, user01.getId(), 48);
		
		Leaderboard week3 = service.getLeaderboard(set.getId(), cycle, OffsetDateTime.of(2018, 06, 14, 0, 0, 0, 0, localOffset));
		LeaderboardTestUtil.assertScoreboards(week3);
		LeaderboardTestUtil.assertScore(week3, user01.getId(), 48);
	}
	
	
	@Test
	public void autoRenewMonthly() {
		
		Cycle cycle = Cycle.Monthly;
		
		// Create a leaderboard set, and set autoRenew
		LeaderboardSet set = service.createSet(title, cycle);
		service.getLeaderboard(set.getLeaderboard(cycle)).setAutoRenew(true);
		
		// Add users
		service.addUser(set.getId(), user01);
		service.addUser(set.getId(), user02);
		service.addUser(set.getId(), user03);

		ZoneOffset localOffset = OffsetDateTime.now().getOffset();
		for (int month = 6; month < 9; month++) {
			for (int day = 1; day < 16; day+=7) {
				for (int hour = 0; hour < 3; hour++) {
					OffsetDateTime time = OffsetDateTime.of(2018, month, day, hour, 0, 0, 0, localOffset);
					service.update(set.getId(), user01.getId(), new Scorable(category, 1, time.plusMinutes(1)));
				}
			}
		}

		// Assert historical leaderboards
		Leaderboard month1 = service.getLeaderboard(set.getId(), cycle, OffsetDateTime.of(2018, 6, 1, 0, 0, 0, 0, localOffset));
		LeaderboardTestUtil.assertScoreboards(month1);
		LeaderboardTestUtil.assertScore(month1, user01.getId(), 9);
		
		Leaderboard month2 = service.getLeaderboard(set.getId(), cycle, OffsetDateTime.of(2018, 7, 7, 0, 0, 0, 0, localOffset));
		LeaderboardTestUtil.assertScoreboards(month2);
		LeaderboardTestUtil.assertScore(month2, user01.getId(), 9);
		
		Leaderboard month3 = service.getLeaderboard(set.getId(), cycle, OffsetDateTime.of(2018, 8, 14, 0, 0, 0, 0, localOffset));
		LeaderboardTestUtil.assertScoreboards(month3);
		LeaderboardTestUtil.assertScore(month3, user01.getId(), 9);
	}
	
	
	@Test
	public void autoRenewCustom() {
		
		// Create a leaderboard set, and set autoRenew
		LeaderboardSet set = service.createSet(title, 
				Cycle.Daily, 
				Cycle.Weekly, 
				Cycle.Monthly, 
				Cycle.Custom);
		
		// Add users
		service.addUser(set.getId(), user01);
		service.addUser(set.getId(), user02);
		service.addUser(set.getId(), user03);

		ZoneOffset localOffset = OffsetDateTime.now().getOffset();
		for (int year = 2018; year < 2021; year++) {
			for (int month = 6; month < 9; month++) {
				for (int day = 1; day < 16; day+=7) {
					for (int hour = 0; hour < 3; hour++) {
						OffsetDateTime time = OffsetDateTime.of(year, month, day, hour, 0, 0, 0, localOffset);
						service.update(set.getId(), user01.getId(), new Scorable(category, 1, time.plusMinutes(1)));
					}
				}
			}
		}
		
//		// Assert historical leaderboards
//		Leaderboard month1 = service.getLeaderboard(set.getId(), cycle, OffsetDateTime.of(2018, 6, 1, 0, 0, 0, 0, localOffset));
//		LeaderboardTestUtil.assertScoreboards(month1);
//		LeaderboardTestUtil.assertScore(month1, user01.getId(), 9);
//		
//		Leaderboard month2 = service.getLeaderboard(set.getId(), cycle, OffsetDateTime.of(2018, 7, 7, 0, 0, 0, 0, localOffset));
//		LeaderboardTestUtil.assertScoreboards(month2);
//		LeaderboardTestUtil.assertScore(month2, user01.getId(), 9);
//		
//		Leaderboard month3 = service.getLeaderboard(set.getId(), cycle, OffsetDateTime.of(2018, 8, 14, 0, 0, 0, 0, localOffset));
//		LeaderboardTestUtil.assertScoreboards(month3);
//		LeaderboardTestUtil.assertScore(month3, user01.getId(), 9);
	}

}
