package com.bytes.fmk.service.leaderboard;

import org.junit.Assert;
import org.junit.Test;

public class LeaderboardSetServiceTest extends LeaderboardServiceTest {

	private String title = "Highscore";
	private LeaderboardSet set;
	
	@Test
	public void createSet() {
		
		set = service.createSet(title, Cycle.Daily, Cycle.Weekly, Cycle.Monthly);
				
		Leaderboard daily = service.getLeaderboard(set.getLeaderboard(Cycle.Daily));
		Leaderboard weekly = service.getLeaderboard(set.getLeaderboard(Cycle.Weekly));
		Leaderboard monthly = service.getLeaderboard(set.getLeaderboard(Cycle.Monthly));
		
		// Check title
		Assert.assertEquals("Title", title, daily.getTitle());
		Assert.assertEquals("Title", title, weekly.getTitle());
		Assert.assertEquals("Title", title, monthly.getTitle());
		
		// Check scoreboards
		LeaderboardTestUtil.assertScoreboards(daily);
		LeaderboardTestUtil.assertScoreboards(weekly);
		LeaderboardTestUtil.assertScoreboards(monthly);
	}
	
	
	@Test
	public void addUsersToSet() {
		
		set = service.createSet(title, Cycle.Daily, Cycle.Weekly, Cycle.Monthly);
		Leaderboard daily = service.getLeaderboard(set.getLeaderboard(Cycle.Daily));
		Leaderboard weekly = service.getLeaderboard(set.getLeaderboard(Cycle.Weekly));
		Leaderboard monthly = service.getLeaderboard(set.getLeaderboard(Cycle.Monthly));
		
		// Register users
		service.addUser(set.getId(), user01);
		service.addUser(set.getId(), user02);
		service.addUser(set.getId(), user03);

		assertUsers(daily);
		assertUsers(weekly);
		assertUsers(monthly);
	}
	
	@Test
	public void updateSet() {

		set = service.createSet(title, Cycle.Daily, Cycle.Weekly, Cycle.Monthly);
		// Register users
		service.addUser(set.getId(), user01);
		service.addUser(set.getId(), user02);
		service.addUser(set.getId(), user03);
				
		// Check {daily, weekly and monthly}
		Leaderboard daily = service.getLeaderboard(set.getLeaderboard(Cycle.Daily));
		Leaderboard weekly = service.getLeaderboard(set.getLeaderboard(Cycle.Weekly));
		Leaderboard monthly = service.getLeaderboard(set.getLeaderboard(Cycle.Monthly));
		
		// Update daily
		service.update(daily.getId(), user01.getId(), Rewardable.ONE_KARMA);
		LeaderboardTestUtil.assertScore(daily, user01.getId(), 1);
		LeaderboardTestUtil.assertScore(weekly, user01.getId(), 1);
		LeaderboardTestUtil.assertScore(monthly, user01.getId(), 1);
		LeaderboardTestUtil.assertScore(weekly, user02.getId(), 0);
		LeaderboardTestUtil.assertScore(monthly, user03.getId(), 0);

		// Update weekly
		service.update(weekly.getId(), user02.getId(), Rewardable.ONE_KARMA);
		LeaderboardTestUtil.assertScore(daily, user01.getId(), 1);
		LeaderboardTestUtil.assertScore(daily, user02.getId(), 1);
		LeaderboardTestUtil.assertScore(weekly, user02.getId(), 1);
		LeaderboardTestUtil.assertScore(monthly, user02.getId(), 1);
		LeaderboardTestUtil.assertScore(monthly, user03.getId(), 0);

		// Update monthly
		service.update(monthly.getId(), user03.getId(), Rewardable.ONE_KARMA);
		LeaderboardTestUtil.assertScore(daily, user01.getId(), 1);
		LeaderboardTestUtil.assertScore(weekly, user02.getId(), 1);
		LeaderboardTestUtil.assertScore(daily, user03.getId(), 1);
		LeaderboardTestUtil.assertScore(weekly, user03.getId(), 1);
		LeaderboardTestUtil.assertScore(monthly, user03.getId(), 1);
	}
	
	/**
	 * Asserts that the specified leaderboard has the expected users.
	 * 1. user01
	 * 2. user02
	 * 3. user03
	 * @param leaderboard
	 */
	private void assertUsers(Leaderboard leaderboard) {

		Assert.assertTrue(leaderboard.getId() + " contains user ID: " + user01.getDisplayName(), leaderboard.getUsers().contains(user01.getId()));
		Assert.assertTrue(leaderboard.getId() + " contains user ID: " + user02.getDisplayName(), leaderboard.getUsers().contains(user02.getId()));
		Assert.assertTrue(leaderboard.getId() + " contains user ID: " + user03.getDisplayName(), leaderboard.getUsers().contains(user03.getId()));
		Assert.assertEquals("User count", 3, leaderboard.getUsers().size());
		
	}
}
