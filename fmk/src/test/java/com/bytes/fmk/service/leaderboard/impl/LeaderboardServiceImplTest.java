package com.bytes.fmk.service.leaderboard.impl;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.bytes.fmk.data.model.User;
import com.bytes.fmk.service.leaderboard.Cycle;
import com.bytes.fmk.service.leaderboard.Scorable;
import com.bytes.fmk.service.leaderboard.Score;
import com.bytes.fmk.service.leaderboard.ScoreboardType;


public class LeaderboardServiceImplTest {
	
	private static final int MAX_USERS = 50;
	
	private String categoryName = "Karma";
	private String leaderboardTitle = "Thinkr Leaderboard";
	
	private LeaderboardServiceImpl leaderboardLogic;
	private LeaderboardImpl leaderboard;
	private String leaderboardId;
	
	
	/**
	 * Register 50 users and generate scores
	 */
	@Before
	public void setup() {
		
		leaderboardLogic = new LeaderboardServiceImpl();
		leaderboard = leaderboardLogic.create(leaderboardTitle, Cycle.Monthly);
		leaderboard.addCategories(categoryName);
		leaderboardId = leaderboard.getId();
		
		for (int i = 0; i < MAX_USERS; i++) {
			leaderboardLogic.addUser(leaderboardId, new User("userId"+(i+1)));
			leaderboardLogic.update(leaderboardId, "userId"+(i+1), new Scorable(categoryName, (i*2)));
		}
		leaderboardLogic.calculate(leaderboardId);
	}

	/**
	 * Create a new user, add score (first), and list my rank
	 */
	@Test
	public void basicFlowFirst() {
		
		String userId = "First";
		leaderboardLogic.addUser(leaderboardId, new User(userId));
		leaderboardLogic.update(leaderboardId, userId, new Scorable(categoryName, MAX_USERS * 3));
		leaderboardLogic.calculate(leaderboardId);
		
		int firstRank = 1;
		Score score = leaderboard.getScore(userId, categoryName);
		Assert.assertEquals(firstRank, leaderboardLogic.getRank(leaderboardId, LeaderboardImpl.TOTAL, userId));
		Assert.assertEquals(userId, score.getEntryId());
	}
	
	/**
	 * Create a new user, add score (last), and list my rank
	 */
	@Test
	public void basicFlowLast() {
		
		String userId = "Last";
		leaderboardLogic.addUser(leaderboardId, new User(userId));
		leaderboardLogic.update(leaderboardId, userId, new Scorable(categoryName, 1));
		leaderboardLogic.calculate(leaderboardId);
		
		int lastRank = MAX_USERS;
		Score score = leaderboard.getScore(userId, categoryName);
		Assert.assertEquals(lastRank, leaderboardLogic.getRank(leaderboardId, LeaderboardImpl.TOTAL, userId));
		Assert.assertEquals(userId, score.getEntryId());
	}
	
	
	/**
	 * Retrieve the list of ranks specified by the starting placement. 
	 * The size of the list is bounded by the count value. 
     * <p>Testing case 1
	 * <li>case 1: {@code getAscending(1,      10) // returns rank 1-10    } </li>
     * <li>case 2: {@code getAscending(1,     MAX) // returns first rank   } </li>
	 * <li>case 3: {@code getAscending(MAX+X, MAX) // returns last rank    } </li>
	 * </p>
	 */
	@Test
	public void getDescending() {
		
		int startRank = 1;
		int count = 10;
		leaderboardLogic.calculate(leaderboardId);
		
		List<Score> result = leaderboardLogic.listDescending(leaderboardId, startRank, count);
		Assert.assertEquals(count, result.size());
		for (int i = 0 ; i < count; i++) {
			Score score = result.get(i);
			Assert.assertEquals(startRank + i, leaderboardLogic.getRank(leaderboardId, LeaderboardImpl.TOTAL, score.getEntryId()));
		}
	}
	

	/**
	 * Retrieve the list of ranks specified by the starting placement. 
	 * The size of the list is bounded by the count value. 
     * <p>Testing case 2
	 * <li>case 1: {@code getAscending(1,      10) // returns rank 1-10    } </li>
     * <li>case 2: {@code getAscending(1,     MAX) // returns first rank   } </li>
	 * <li>case 3: {@code getAscending(MAX+X, MAX) // returns last rank    } </li>
	 * </p>
	 */
	@Test
	public void getDescendingAll() {
		
		int startRank = 1;
		int count = MAX_USERS;
		leaderboardLogic.calculate(leaderboardId);
		
		List<Score> result = leaderboardLogic.listDescending(leaderboardId, startRank, count);
		Assert.assertEquals(count, result.size());
		for (int i = 0 ; i < count; i++) {
			Score score = result.get(i);
			Assert.assertEquals(1 + i, leaderboardLogic.getRank(leaderboardId, LeaderboardImpl.TOTAL, score.getEntryId()));
		}
	}	
	
	/**

	 * Retrieve the list of ranks specified by the starting placement. 
	 * The size of the list is bounded by the count value.
	 * Request for the n-ranks below rank max+1. 
	 * This should default to return 1 entry, the last ranked entry.  
     * <p>Testing case 3
	 * <li>case 1: {@code getAscending(1,      10) // returns rank 1-10    } </li>
     * <li>case 2: {@code getAscending(1,     MAX) // returns first rank   } </li>
	 * <li>case 3: {@code getAscending(MAX+X, MAX) // returns last rank    } </li>
	 * </p>
	 */
	@Test
	public void getDescendingOutOfRange() {
		
		int startRank = MAX_USERS +1;
		int count = MAX_USERS;
		leaderboardLogic.calculate(leaderboardId);
		
		List<Score> result = leaderboardLogic.listDescending(leaderboardId, startRank, count);
		Assert.assertEquals(1, result.size());
		for (int i = 0 ; i < 1; i++) {
			Score score = result.get(i);
			Assert.assertEquals(MAX_USERS + i, leaderboardLogic.getRank(leaderboardId, LeaderboardImpl.TOTAL, score.getEntryId()));
		}
	}	
	
	/**
	 * Retrieve the list of ranks specified by the ending placement. 
	 * The size of the list is bounded by the count value. 
	 * <p>Testing case 1
	 * <li>case 1: {@code getAscending(1,      10)   // returns rank 1       } </li>
	 * <li>case 2: {@code getAscending(MAX,   MAX)   // returns all entries  } </li>
	 * <li>case 3: {@code getAscending(MAX+X, MAX)   // returns all entries  } </li>
	 * <li>case 4: {@code getAscending(MAX/2, MAX/2) // returns bottom half  } </li>
	 * </p>
	 */
	@Test
	public void getAscending() {
		
		int startRank = 1;
		int count = 10;
		leaderboardLogic.calculate(leaderboardId);
		
		List<Score> result = leaderboardLogic.listAscending(leaderboardId, ScoreboardType.User, LeaderboardImpl.TOTAL, startRank, count);
		Assert.assertEquals(1, result.size());
		for (int i = 0 ; i < 1; i++) {
			Score score = result.get(i);
			Assert.assertEquals(1, leaderboardLogic.getRank(leaderboardId, LeaderboardImpl.TOTAL, score.getEntryId()));
		}
	}	
	
	/**
	 * Retrieve the list of ranks specified by the ending placement. 
	 * The size of the list is bounded by the count value. 
	 * <p>Testing case 2
	 * <li>case 1: {@code getAscending(1,      10) // returns rank 1       } </li>
	 * <li>case 2: {@code getAscending(MAX,   MAX) // returns all entries  } </li>
	 * <li>case 3: {@code getAscending(MAX+X, MAX) // returns all entries  } </li>
	 * </p>
	 */
	@Test
	public void getAscendingAll() {
		
		int startRank = MAX_USERS;
		int count = MAX_USERS;
		leaderboardLogic.calculate(leaderboardId);
		
		List<Score> result = leaderboardLogic.listAscending(leaderboardId, ScoreboardType.User, LeaderboardImpl.TOTAL, startRank, count);
		Assert.assertEquals(count, result.size());
		for (int i = 0 ; i < count; i++) {
			Score score = result.get(i);
			Assert.assertEquals((startRank+1 - count) + i, leaderboardLogic.getRank(leaderboardId, LeaderboardImpl.TOTAL, score.getEntryId()));
		}
	}	
	
	/**
	 * Retrieve the list of ranks specified by the ending placement. 
	 * The size of the list is bounded by the count value. 
	 * <p>Testing case 3
	 * <li>case 1: {@code getAscending(1,      10) // returns rank 1       } </li>
	 * <li>case 2: {@code getAscending(MAX,   MAX) // returns all entries  } </li>
	 * <li>case 3: {@code getAscending(MAX+X, MAX) // returns all entries  } </li>
	 * </p>
	 */
	@Test
	public void getAscendingOutOfRange() {
		
		int startRank = MAX_USERS +10;
		int count = MAX_USERS;
		leaderboardLogic.calculate(leaderboardId);
		
		List<Score> result = leaderboardLogic.listAscending(leaderboardId, ScoreboardType.User, LeaderboardImpl.TOTAL, startRank, count);
		Assert.assertEquals(MAX_USERS, result.size());
		for (int i = 0 ; i < MAX_USERS; i++) {
			Score score = result.get(i);
			Assert.assertEquals(1 + i, leaderboardLogic.getRank(leaderboardId, LeaderboardImpl.TOTAL, score.getEntryId()));
		}
	}	
	
	/**
	 * Retrieve the list of ranks specified by the ending placement. 
	 * The size of the list is bounded by the count value. 
	 * <p>Testing case 4
	 * <li>case 4: {@code getAscending(MAX/2, MAX/2) // returns top half  } </li>
	 * </p>
	 */
	@Test
	public void getAscending4() {
		
		int startRank = MAX_USERS/2;
		int count = MAX_USERS/2;
		leaderboardLogic.calculate(leaderboardId);
		
		List<Score> result = leaderboardLogic.listAscending(leaderboardId, ScoreboardType.User, LeaderboardImpl.TOTAL, startRank, count);
		Assert.assertEquals(MAX_USERS/2, result.size());
		for (int i = 0 ; i < count; i++) {
			Score score = result.get(i);
			Assert.assertEquals(1+i, leaderboardLogic.getRank(leaderboardId, LeaderboardImpl.TOTAL, score.getEntryId()));
		}
	}	
	
	/**
	 * Retrieve the list of ranks specified by the ending placement. 
	 * The size of the list is bounded by the count value. 
	 * <p>Testing case 5
	 * <li>case 4: {@code getAscending(MAX, MAX/2) // returns bottom half  } </li>
	 * </p>
	 */
	@Test
	public void getAscending5() {
		
		int startRank = MAX_USERS;
		int count = MAX_USERS/2;
		leaderboardLogic.calculate(leaderboardId);
		
		List<Score> result = leaderboardLogic.listAscending(leaderboardId, ScoreboardType.User, LeaderboardImpl.TOTAL, startRank, count);
		
		// Debug
		result.forEach(score -> System.out.println("Rank " + leaderboardLogic.getRank(leaderboardId, LeaderboardImpl.TOTAL, score.getEntryId())));
		
		Assert.assertEquals(count, result.size());
		for (int i = 0 ; i < count; i++) {
			Score score = result.get(i);
			Assert.assertEquals(MAX_USERS/2 + i+1, leaderboardLogic.getRank(leaderboardId, LeaderboardImpl.TOTAL, score.getEntryId()));
		}
	}	
	
	
	/**
	 * Retrieve the list of ranks specified by the user id. 
	 * The size of the list is bounded by the count value. 
	 * <p>Testing case 1
	 * <li>case 1: {@code getAscending(MAX, MAX/2) // returns bottom half  } </li>
	 * </p>
	 */
	@Test
	public void getCurrentRank() {
		
		String userId = "userId20";
		int count = 3;
		leaderboardLogic.calculate(leaderboardId);
		
		List<Score> result = leaderboardLogic.listRanks(leaderboardId, ScoreboardType.User, LeaderboardImpl.TOTAL, userId, count);
		
		// Debug
		int rank = leaderboardLogic.getRank(leaderboardId, LeaderboardImpl.TOTAL, userId);
		 result.forEach(score -> System.out.println("Rank " + leaderboardLogic.getRank(leaderboardId, LeaderboardImpl.TOTAL, score.getEntryId())));
		
		Assert.assertEquals(count*2, result.size());

		int i = rank-count;
		for (Score score : result) {
			Assert.assertEquals(
					i++, 
					leaderboardLogic.getRank(leaderboardId, LeaderboardImpl.TOTAL, score.getEntryId()));
		}
	}
}
