package com.bytes.fmk.service.leaderboard;

import java.time.OffsetDateTime;
import java.util.Collection;

import com.bytes.fmk.service.persistence.Persistable;

/**
 * This represent a grouping of Leaderboards where all of their statistical data are 
 * synchronized. The difference is their calculation cycles.
 * <ul>It is expected that a Leaderboard set consists of the following:
 * <li>The same {@code User}s
 * <li>the same {@code ScoreCategory}
 * <li>the same {@code Scoreboard}
 * <li>different {@code Cycle}
 * </ul>
 * 
 * @author Kent
 */
public interface LeaderboardSet extends Persistable<LeaderboardSet> {
	
	/**
	 * Request to add the leaderboards to this set
	 * @param leaderboards the leaderboards to be added
	 * @return true if successfully added, false otherwise
	 */
	boolean add(Leaderboard...leaderboards);
	
	
	/**
	 * Get the {@code Leaderboard} matching the specified cycle
	 * @param cycle - the cycle
	 * @return the leaderboard id, null if not found
	 */
	String getLeaderboard(Cycle cycle);
	
	
	/**
	 * Returned the leaderboard id for the leaderboard in this set that has 
	 * the matching cycle
	 * @param leaderboard - the leaderboard within the set
	 * @return the structured id - can be use to lookup in persistence store
	 */
	String generateLeaderboardId(Leaderboard leaderboard);

	
	/**
	 * Returned the leaderboard id for the leaderboard in this set that has 
	 * the matching cycle and time
	
	 * @param cycle - the cycle
	 * @param date - the applicable time
	 * @return the structured id - can be use to lookup in persistence store
	 */
	String generateLeaderboardId(Cycle cycle, OffsetDateTime date);
		
	
	/**
	 * Get all {@code Leaderboard} ids in this set
	 * @return
	 */
	Collection<String> getAll();


	/**
	 * Get the Leaderboard set id
	 * @return
	 */
	String getId();

}
