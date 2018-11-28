package com.bytes.fmk.service.leaderboard;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;

import com.bytes.fmk.service.leaderboard.impl.Scoreboard;

/**
 * This will create a Leaderboard and register in the Service cache, allowing it
 * to be use in other threads without passing its reference.
 * 
 * <p>
 * Creating a Leaderboard
 * 
 * <pre>
 * LeaderboardService service = Thinkr.INSTANCE.getLeaderboardService(); 
 * Leaderboard leaderboard = service.create(title, scoreName);
 * String leaderboardId = service.register(leaderboard)
 * </pre>
 * 
 * <p>
 * Using it subsequently
 * 
 * <pre>
 * LeaderboardService service = Thinkr.INSTANCE.getLeaderboardService();
 * Leaderboard leaderboard = service.getLeaderboard(leaderboardId);
 * </pre>
 * 
 * @author Kent
 */
public interface Leaderboard {

	
	/**
	 * This is the default score category for all leaderboard The default score
	 * category name for the total points.
	 */
	static final String TOTAL = "Total";

	
	/**
	 * Retrieve the score and rank of the specified user
	 * 
	 * @param userId - the id of the user
	 * @return the {@code Score} corresponding to the specified user
	 */
	Score getScoreTotal(String userId);

	
	/**
	 * Retrieve the score and rank of the specified user
	 * 
	 * @param userId - the id of the user
	 * @param category - the specific category
	 * @return the {@code Score} corresponding to the specified user
	 */
	Score getScore(String userId, String categoryName);

	
	/**
	 * Retrieve scores in all category for a specific user. The scores in this map
	 * is not ranked.
	 * @param userId - the id of the user
	 * @return the {@code Score}s corresponding to the specified user, keyed by
	 *         score category name. Returns an empty map if userId is not found.
	 */
	Map<String, Score> getScores(String userId);

	
	/**
	 * Add the specified category
	 * @param category - the categories to add
	 */
	void addCategories(String... categoryNames);

	
	/**
	 * Indicates if the category exists.
	 * @param categoryName - the name of the score category
	 * @return true if it exists
	 */
	boolean hasCategory(String categoryName);

	
	/**
	 * Get the leaderboard auto-generated identifier. 
	 * @return the leaderboard id
	 */
	String getId();

	
	
	/**
	 * Get the ledger id corresponding to this leaderboard.
	 * @return the ledger id
	 */
	String getLedgerId();
	

	/**
	 * Get the leaderboard description
	 * @return the description
	 */
	String getDescription();

	
	/**
	 * Set the description for this leaderboard
	 * @param description the leaderboard description
	 */
	void setDescription(String description);

	
	/**
	 * Get the leaderboard title
	 * @return the title
	 */
	String getTitle();

	
	/**
	 * Get the effective start date of this leaderboard.
	 * Any activities recorded before the start date are not counted. 
	 * @return the start date
	 */
	OffsetDateTime getStartDate();

	
	/**
	 * Set the leaderboard start date. The leaderboard must be 
	 * recalculated to update the scoring. 
	 * @param startDate - the start date
	 */
	void setStartDate(OffsetDateTime startDate);

	
	/**
	 * Get the end date of this leaderboard.
	 * Any activities recorded after the end date are not counted
	 * @return the end date
	 */
	OffsetDateTime getEndDate();

	
	/**
	 * Indicates if this leaderboard is currently active.
	 * A leaderboard is active if the current time is within its start and end time.
	 * @param time 
	 * @return true if active, false otherwise
	 */
	boolean isActive(OffsetDateTime time);
	
	/**
	 * Set the leaderboard end date. The leaderboard must be 
	 * recalculated to update the scoring. 
	 * @param endDate - the end date
	 */
	void setEndDate(OffsetDateTime endDate);

	
	/**
	 * If set, calculation will be performed on every update and 
	 * data is saved to default persistence store. 
	 * This is on by default.
	 *  
	 * Not recommended unless it's a small size, < 100 entries 
	 * and relative low update volume (i.e., < 10 per minute)
	 * @return
	 */
	boolean isAutoUpdate();
	

	/**
	 * Set the auto update flag
	 * @param autoUpdate - the autoUpdate flag
	 */
	void setAutoUpdate(boolean autoUpdate);

	

	/**
	 * If set, data is saved to the persistence store on every update.
	 * The persistence store determined by the service implementation.
	 * This is off by default.
	 * 
	 * @return true if the data flag is set
	 */
	boolean isAutoPersist();
	
	
	/**
	 * Set the auto persist flag
	 * @param autoPersist - the autoPersist flag
	 */
	void setAutoPersist(boolean autoPersist);
	
	
	/**
	 * Get the leaderboard category name
	 * @return the leaderboard category name
	 */
	Set<String> getCategoryNames();

	
	/**
	 * Get the number of users participating in this leaderboard.
	 * @return the number of users in this leaderboard.
	 */
	int getSize();

	
	/**
	 * Get all registered users.
	 * @return the set of all registered user ids.
	 */
	Set<String> getUsers();
	
	/**
	 * Get the leaderboard reward ranking Cycle
	 * @return the reward ranking cycle
	 */
	Cycle getCycle();
	
	
	/**
	 * Request to get the {@code Scoreboard}
	 * @return the map of scoreboards 
	 */
	Map<ScoreboardType, Scoreboard> getScoreboards();


	/**
	 * Get the LeaderboardSet id associated with this leaderboard.
	 * @return the leaderboard set id, null if not in a set
	 */
	public String getGroupId();

	/**
	 * Set the {@code LeaderboardSet} id
	 * @param id - the id
	 */
	public void setGroupId(String groupId);


	/**
	 * 
	 * @param isAutoRenew
	 */
	void setAutoRenew(boolean isAutoRenew);
	
	
	/**
	 * 
	 * @return
	 */
	boolean isAutoRenew();

}
