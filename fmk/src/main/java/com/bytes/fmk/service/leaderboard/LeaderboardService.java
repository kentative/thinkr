package com.bytes.fmk.service.leaderboard;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import com.bytes.fmk.data.model.User;
import com.bytes.fmk.service.ThinkrService;
import com.bytes.fmk.service.leaderboard.ledger.Ledger;
import com.bytes.fmk.service.leaderboard.ledger.LedgerEntry;
import com.bytes.fmk.service.leaderboard.ledger.Recordable;
import com.bytes.fmk.service.leaderboard.report.Report;
import com.bytes.fmk.service.leaderboard.report.ReportBuilder;
import com.bytes.fmk.service.persistence.Persistable;
import com.bytes.fmk.service.persistence.PersistenceMode;

public interface LeaderboardService extends ThinkrService, Persistable<LeaderboardService> {
	
	
	/**
	 * Request to create a {@code LeaderboardSet}.
	 * A leaderboard set consists of Leaderboards with the same 
	 * title and statistical data, differs only in {@code Cycle}.
	 * An update to one of the leaderboard in the set will update all 
	 * applicable leaderboards. For example, updating a daily leaderboard will also
	 * update its weekly and monthly counterpart if they are all in set.
	 * 
	 * @param title - the shared title
	 * @param cycles - the list of support cycles
	 * @return
	 */
	LeaderboardSet createSet(String title, Cycle...cycles);

		
	/**
	 * Request to create and register a leaderboard
	 * 
	 * @param title - the leaderboard title
	 * @param cycle - the reward cycle of the leaderboard
	 * @return the {@code Leaderboard} with a unique id
	 */
	Leaderboard create(String title, Cycle cycle);
	
	
	/**
	 * Retrieve the list of ranks specified by the starting placement. 
	 * The size of the list is bounded by the count value. 
	 * <p>For example with total entries = 50:
	 * <li>{@code getDescending(1, 10)  // returns rank 1-10 rank } </li>
	 * <li>{@code getDescending(1, 100) // returns rank 1-50      } </li>
	 * <li>{@code getDescending(45, 10) // returns rank 45-50     } </li>
	 * </p>
	 * 
	 * @param leaderboardId - the leaderboard id
	 * @param type - the scoreboard type
	 * @param categoryName - list the rank based on this score category
	 * @param start - the starting placement, 1 is first -- don't use 0
	 * @param count - the total entries
	 * @return the requested list of scores
	 */
	public List<Score> listDescending(String leaderboardId, ScoreboardType type, String categoryName, int start, int count);
	
	
	/**
	 * Retrieve the list of ranks specified by the ending placement. 
	 * The size of the list is bounded by the count value. 
	 * <p>For example with total entries = 50:
	 * <li>{@code getAscending(1, 10)  // returns rank 1         } </li>
	 * <li>{@code getAscending(10, 10) // returns rank 1-10      } </li>
	 * <li>{@code getAscending(45, 10) // returns rank 35-45     } </li>
	 * </p>
	 * 
	 * @param leaderboardId - the leaderboard id
	 * @param type - the scoreboard type
	 * @param categoryName - list the rank based on this score category
	 * @param start - the starting placement, 1 is first -- don't use 0
	 * @param count - at least 1 or greater
	 * @return the requested list of scores
	 */
	public List<Score> listAscending(String leaderboardId, ScoreboardType type, String categoryName, int start, int count);
	
	
	/**
	 * Retrieve the list of ranks nearby the specified user's rank
	 * <p>For example with total entries = 50:
	 * <li>{@code getCurrentRank(20, 5)  // returns rank 15-25} </li>
	 * </p>
	 * 
	 * @param leaderboardId - the leaderboard id
	 * @param type - the scoreboard type
	 * @param categoryName - list the rank based on this score category
	 * @param userId - the user id
	 * @param count - the number of entries in front and behind of the user's rank
	 * @return the requested list of scores
	 */
	public List<Score> listRanks(String leaderboardId, ScoreboardType type, String categoryName, String userId, int count);
	
	
	/**
	 * Perform rank calculation based on total points in all score categories.
	 * Rank is only calculated on request, unless {@code Leaderboard#isAutoUpdate()} is enabled.
	 * It is disabled by default.  
	 * @param leaderboardId - the leaderboard id
	 */
	public void calculate(String leaderboardId);
	
	
	/**
	 * Perform rank calculation based on a specific score category.
	 * Rank is only calculated on request, unless {@code Leaderboard#isAutoUpdate()} is enabled. 
	 * It is disabled by default.  
	 * 
	 * @param leaderboardId - the leaderboard id
	 * @param categoryName - the score category name
	 */
	public void calculate(String leaderboardId, String categoryName);
	
	
	/**
	 * Request to update the leaderboard with a {@code Recordable}
	 * Leaderboard is only update if it is active during the time of the recordable.
	 * If the recordable time is null, it is defaulted to now.
	 * 
	 * @param id - the leaderboard or the leaderboard set id
	 * @param userId - the user id
	 * @param recordable - the {@code Rewardable} action
	 * @return true if successful
	 */
	public boolean update(String id, String userId, Recordable recordable);
	
	
	/**
	 * Request to update the leaderboard with the list of ledger entries.
	 * 
	 * @param id - leaderboard id
	 * @param recordEntries - list of ledger entries grouped by user id
	 */
	public boolean update(String id, Map<String, List<LedgerEntry>> recordEntries);
	
	
	/**
	 * Request to register the user in the leaderboard.
	 * 
	 * @param id - the leaderboard or leaderboardSet id
	 * @param user - the user
	 * @return true if successful
	 */
	public boolean addUser(String id, User user);
	
	
	/**
	 * Request to unregister the user from the leaderboard
	 * @param leaderboardId - the leaderboard id
	 * @param userId
	 * @param score
	 * @return true if successful
	 */
	public boolean removeUser(String leaderboardId, String userId);
	
	
	/**
	 * Request to save leaderboard states to external cache
	 * @param leaderboard - the leaderboard
	 * @param mode - the persistence mode
	 * @return true if successfully persisted
	 */
	public boolean save(Leaderboard leaderboard, PersistenceMode mode);
	
	
	/**
	 * Request to load leaderboard states from external cache
	 * @param leaderboardId - the leaderboard id
	 * @param mode - the persistence mode
	 * @return the loaded {@code Leaderboard}. If failed, returns null
	 */
	public Leaderboard load(String leaderboardId, PersistenceMode mode);

	
	/**
	 * Get the rank of the specified user
	 * @param leaderboardId - the leaderboard id
	 * @param categoryName - the category name
	 * @param userId - the user id
	 * @return the user rank, -1 if user is not registered
	 */
	public int getRank(String leaderboardId, String categoryName, String userId);

	
	/**
	 * Retrieve the {@code Leaderboard} corresponding to the specified id
	 * @param id - the leaderboard id, if only 1 is registered, 
	 * a null value will return that leaderboard.
	 * @return the Leaderboard if found, null otherwise
	 */
	public Leaderboard getLeaderboard(String id);
	
	
	/**
	 * Retrieve the {@code LeaderboardSet} corresponding to the specified id
	 * @param id - the leaderboard set id
	 * @return the leaderboard set if found, null otherwise
	 */
	public LeaderboardSet getLeaderboardSet(String id);
	
	
	
	/**
	 * Request to get the ledger entries containing all of the recordable events 
	 * going through this service.
	 * @return the leaderboard ledger
	 */
	public Ledger getLedger();
	
	
		
	/**
	 * Generates a {@code Report}
	 * @param leaderboardId - the leaderboard id
	 * @param builder - the default report builder
	 * @return the leaderboard report
	 */
	public Report generateReport(String leaderboardId, ReportBuilder builder);
	
	
	/**
	 * Request to merge the user score maps from the leaderboard. 
	 * @param sourceId - the id of the source leaderboard
	 * @param sourceMode - the {@code PersistenceMode} of the source leaderboard
	 * @param targetId - the id of the target leaderboard 
	 * @param targetMode - the {@code PersistenceMode} of the target leaderboard
	 * @return the target leaderboard containing the data of both leaderboards
	 */
	public Leaderboard merge(String sourceId, PersistenceMode sourceMode, String targetId, PersistenceMode targetMode);


	/**
	 * Get the {@code Leaderboard} corresponding to the specified set, cycle and date
	 * @param id - the {@code LeaderboardSet} id
	 * @param cycle - the cycle
	 * @param time - the time
	 */
	public Leaderboard getLeaderboard(String id, Cycle cycle, OffsetDateTime time);

}
