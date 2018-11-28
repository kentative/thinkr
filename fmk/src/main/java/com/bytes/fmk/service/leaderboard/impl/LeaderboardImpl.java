package com.bytes.fmk.service.leaderboard.impl;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bytes.fmk.service.leaderboard.Cycle;
import com.bytes.fmk.service.leaderboard.Leaderboard;
import com.bytes.fmk.service.leaderboard.Score;
import com.bytes.fmk.service.leaderboard.ScoreCategory;
import com.bytes.fmk.service.leaderboard.ScoreboardType;
import com.bytes.fmk.service.leaderboard.impl.util.LeaderboardUtil;

/**
 * This will create a Leaderboard and register in the Service cache, allowing it to be 
 * use in other threads without passing its reference. 
 * 
 * <p> Creating a Leaderboard
 * <pre>
 * LeaderboardService service = Thinkr.INSTANCE.getLeaderboardService(); 
 * Leaderboard leaderboard = service.create(title, scoreName);
 * String leaderboardId = service.register(leaderboard)
 * </pre>
 * 
 * <p> Using it subsequently
 * <pre>
 * LeaderboardService service = Thinkr.INSTANCE.getLeaderboardService();
 * Leaderboard leaderboard = service.getLeaderboard(leaderboardId);
 * </pre>
 * 
 * @author Kent
 */
public class LeaderboardImpl implements Leaderboard {
	
	private static Logger logger = LoggerFactory.getLogger(LeaderboardImpl.class);
	
	private static final String LEDGER_PREFIX = "Ledger-";
	
	/**
	 * The leaderboard unique identifier.
	 * This id is used to persist and retrieve the leaderboard
	 */
	private String id;


	/**
	 * Contains the list of {@code Scoreboard}  
	 * user, team, guild 
	 */
	private Map<ScoreboardType, Scoreboard> scoreboards;
	
	
	/**
	 * The description of the leaderboard
	 */
	private String description;
	
	
	/**
	 * The title of the leaderboard
	 */
	private String title;
	
	
	/**
	 * The start date
	 */
	private OffsetDateTime startDate;
	
	
	/**
	 * The end date
	 */
	private OffsetDateTime endDate;
	
	
	/** 
	 * enable auto recalculation of rank after every score update 
	 **/
	private boolean autoUpdate;
	
	
	/**
	 * enable auto persistence to the data store on update.
	 */
	private boolean autoPersist;
	
	
	/**
	 * If part of a set, this is the {@code LeaderboardSet} id.
	 * null if not part of a set.
	 */
	private String groupId;
	
	
	/**
	 * The leaderboard reward cycle
	 */
	private Cycle cycle;
	
	
	/**
	 * Flag to indicate if the start and end time should auto update
	 * if the cycle duration expired. The default is true.
	 */
	private boolean autoRenew;
	
	
	/**
	 * Constructor. 
	 * Rank is not yet calculated for this leaderboard.
	 */
	public LeaderboardImpl() {
		this("Leaderboard", Cycle.Weekly);
	}
	
	
	/**
	 * Construct the leaderboard with a collection of scores.
	 * Leaderboard is set to be active for 1 day by default.
	 * Rank is not yet calculated for this leaderboard.
	 * @param title  - the leaderboard title
	 * @param cycle - the leaderboard reward cycle 
	 */
	LeaderboardImpl(String title, Cycle cycle) {
		this(title, cycle, OffsetDateTime.now());
	}

	
	/**
	 * Construct the leaderboard with a collection of scores.
	 * Leaderboard is set to be active for 1 day by default.
	 * Rank is not yet calculated for this leaderboard.
	 * @param title  - the leaderboard title
	 * @param cycle - the leaderboard reward cycle 
	 * @param time - the reference time used with the cycle to generate start and end time 
	 */
	public LeaderboardImpl(String title, Cycle cycle, OffsetDateTime time) {
		// Core
		this.id = UUID.randomUUID().toString();
		this.title = title;
		this.cycle = cycle;
		this.scoreboards = new HashMap<>();
		this.autoPersist = false;
		this.autoUpdate = true;
		this.groupId = null;
		this.autoRenew = true;
		
		logger.info("Adding 3 scoreboards by default (User, Team, Guild)");
		scoreboards.put(ScoreboardType.User, new Scoreboard(ScoreboardType.User, "User"));
		scoreboards.put(ScoreboardType.Team, new Scoreboard(ScoreboardType.Team, "Team"));
		scoreboards.put(ScoreboardType.Guild, new Scoreboard(ScoreboardType.Guild, "Guild"));

		setStartAndEndTime(time);
		addCategoryInternal(TOTAL);
	}

	
	/**
	 * Set the start and end time based on this:
	 * start = today but at 00:00
	 * end   = today + cycle duration 00:00
	 * @param atTime - the active time of this leaderboard 
	 */
	void setStartAndEndTime(OffsetDateTime atTime) {
		
		this.startDate = LeaderboardUtil.getStartTime(cycle, atTime);
		switch (cycle) {
		
		case Daily:
			this.endDate = startDate.plus(1, ChronoUnit.DAYS).minusSeconds(1);
			break;

		case Weekly:
			this.endDate = startDate.plus(1, ChronoUnit.WEEKS).minusSeconds(1);
			break;

		case Monthly:
			this.endDate = startDate.plus(1, ChronoUnit.MONTHS).minusSeconds(1);
			break;

		default:
			// Default to 10 year, expects date to be set manually
			this.endDate = startDate.plus(10, ChronoUnit.YEARS).minusSeconds(1);
			break;
		}

		logger.info("Set " + cycle + " Leaderboard default active time: " +  
				" \n  start: " + startDate.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) +
				" \n    end: " + endDate.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
	}

	/**
	 * Retrieve the score and rank of the specified user
	 * @param userId - the id of the user
	 * @return the {@code Score} corresponding to the specified user
	 */
	public Score getScoreTotal(String userId) {
		return getScore(userId, TOTAL);
	}

	
	/**
	 * Retrieve the score and rank of the specified user
	 * @param userId - the id of the user
	 * @param category - the specific category
	 * @return the {@code Score} corresponding to the specified user
	 */
	public Score getScore(String userId, String categoryName) {

		Scoreboard scoreboard = scoreboards.get(ScoreboardType.User);
		return scoreboard.getScore(userId, categoryName);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Score> getScores(String userId) {
		
		Scoreboard scoreboard = scoreboards.get(ScoreboardType.User);
		return scoreboard.getScoreEntries().get(userId);
	}

	/**
	 * Get the rank based on total score
	 * @return the rank of the total
	 */
	int getRank(String userId) {
		return getRank(userId, TOTAL);
	}

	
	/**
	 * Get the rank of the specified score
	 * @param userId - the user id
	 * @param categoryName - the specific category, identified by its name
	 * @return the rank of the specified score, -1 if user is not ranked
	 */
	int getRank(String userId, String categoryName) {
		Scoreboard scoreboard = scoreboards.get(ScoreboardType.User);
		return scoreboard.getRank(userId, categoryName);
	}
	
	
	/**
	 * Add the specified category
	 * @param category - the categories to add
	 */
	public void addCategories(String... categoryNames) {
		for (Scoreboard scoreboard : scoreboards.values()) {
			scoreboard.addCategories(categoryNames);
		}
	}

	
	/**
	 * Indicates if the category exists.
	 * @param categoryName - the name of the score category
	 * @return true if it exists
	 */
	public boolean hasCategory(String categoryName) {
		Scoreboard scoreboard = scoreboards.get(ScoreboardType.User);
		return scoreboard.hasCategory(categoryName);
	}
	
	
	/**
	 * Add a new category. This internal method bypass the 
	 * check to reserved categories.
	 * @param categoryName - the name of the category
	 * @return the {@code ScoreCategory}
	 */
	private ScoreCategory addCategoryInternal(String categoryName) {
		Scoreboard scoreboard = scoreboards.get(ScoreboardType.User);
		return scoreboard.addCategoryInternal(categoryName);
	}

	
	/**
	 * Get the ranked score corresponding to the category
	 * @param categoryName - the category
	 * @return the rankedScores
	 */
	public List<Score> getRankedScores(String categoryName, ScoreboardType type) {
		Scoreboard scoreboard = scoreboards.get(type);
		return scoreboard.getRankedScores(categoryName);
	}
	
	
	/**
	 * Request to update the ranks for the specified category
	 * @param categoryName - the category name
	 * @param scores - the ranked scores
	 */
	void setRankedScores(String categoryName, ArrayList<Score> scores) {
		Scoreboard scoreboard = scoreboards.get(ScoreboardType.User);
		scoreboard.setRankedScores(categoryName, scores);
	}


	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the id
	 */
	public String getLedgerId() {
		return LEDGER_PREFIX + id;
	}
	
	/**
	 * @param id the id to set
	 */
	void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTitle() {
		return title;
	}

	void setTitle(String title) {
		this.title = title;
	}

	public OffsetDateTime getStartDate() {
		return startDate;
	}

	public void setStartDate(OffsetDateTime startDate) {
		this.startDate = startDate;
	}

	public OffsetDateTime getEndDate() {
		return endDate;
	}

	public void setEndDate(OffsetDateTime endDate) {
		this.endDate = endDate;
	}

	Map<String, Map<String, Score>> getUserScores() {
		Scoreboard scoreboard = scoreboards.get(ScoreboardType.User);
		return scoreboard.getScoreEntries();
	}

	void setUserScores(Map<String, Map<String, Score>> userScores) {
		Scoreboard scoreboard = scoreboards.get(ScoreboardType.User);
		scoreboard.setScoreEntries(userScores);
	}

	public boolean isAutoUpdate() {
		return autoUpdate;
	}

	public void setAutoUpdate(boolean autoUpdate) {
		this.autoUpdate = autoUpdate;
	}

	
	/**
	 * Retrieve the category registered to this leaderboard
	 * @param name - the name of the category
	 * @return the ScoreCategory, null if not found
	 */
	ScoreCategory getCategory(String name) {
		Scoreboard scoreboard = scoreboards.get(ScoreboardType.User);
		return scoreboard.getCategory(name);
	}

	Collection<ScoreCategory> getCategories() {
		Scoreboard scoreboard = scoreboards.get(ScoreboardType.User);
		return scoreboard.getCategories();
	}
	
	
	public Set<String> getCategoryNames() {
		Scoreboard scoreboard = scoreboards.get(ScoreboardType.User);
		return scoreboard.getCategoryNames();
	}
	
	/**
	 * Get the number of users participating in this leaderboard.
	 * @return the number of users in this leaderboard.
	 */
	public int getSize() {
		Scoreboard scoreboard = scoreboards.get(ScoreboardType.User);
		return scoreboard.getSize();
		
	}
	
	@Override
	public Set<String> getUsers() {
		Scoreboard scoreboard = scoreboards.get(ScoreboardType.User);
		return scoreboard.getEntries();
	}

	
	public Cycle getCycle() {
		return cycle;
	}

	public Map<ScoreboardType, Scoreboard> getScoreboards() {
		return scoreboards;
	}

	public boolean isActive(OffsetDateTime time) {
		return time.isAfter(getStartDate()) && time.isBefore(getEndDate());
	}

	public boolean isAutoPersist() {
		return autoPersist;
	}

	public void setAutoPersist(boolean autoPersist) {
		this.autoPersist = autoPersist;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public boolean isAutoRenew() {
		return autoRenew;
	}

	public void setAutoRenew(boolean autoRenew) {
		this.autoRenew = autoRenew;
	}

}
