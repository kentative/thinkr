package com.bytes.fmk.service.leaderboard;

import java.util.Map;

import org.junit.Assert;

import com.bytes.fmk.data.model.User;
import com.bytes.fmk.service.Thinkr;
import com.bytes.fmk.service.leaderboard.impl.Scoreboard;

public class LeaderboardTestUtil {


	/**
	 * Assert that the specified leaderboard has the expected score
	 * @param leaderboard
	 * @param userId
	 * @param points
	 */
	public static void assertScore(Leaderboard leaderboard, String userId, int points) {
		User user = Thinkr.INSTANCE.getUserService().getUser(userId);
		Score score = leaderboard.getScore(userId, Rewardable.ONE_KARMA.getCategoryName());
		Assert.assertEquals(leaderboard.getId() + " " + user.getDisplayName() + " Category Name", "Karma", score.getCategoryName());
		Assert.assertEquals(leaderboard.getId() + " " + user.getDisplayName() + " Points", points, score.getPoints());
	}
	
	
	/**
	 * Asserts that the specified leaderboard has the expected scoreboards.
	 * 1. user  - has the default category: Total
	 * 2. team  - has the default category: Total
	 * 3. guild - has the default category: Total
	 * @param leaderboard
	 */
	public static void assertScoreboards(Leaderboard leaderboard) {
		
		Map<ScoreboardType, Scoreboard> scoreboards = leaderboard.getScoreboards();
		Scoreboard userBoard = scoreboards.get(ScoreboardType.User);
		Scoreboard teamBoard = scoreboards.get(ScoreboardType.Team);
		Scoreboard guildBoard = scoreboards.get(ScoreboardType.Guild);
		Assert.assertTrue("Has TOTAL category", userBoard.hasCategory(Leaderboard.TOTAL));
		Assert.assertTrue("Has TOTAL category", teamBoard.hasCategory(Leaderboard.TOTAL));
		Assert.assertTrue("Has TOTAL category", guildBoard.hasCategory(Leaderboard.TOTAL));
	}
}
