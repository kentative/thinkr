package com.bytes.fmk.service.leaderboard.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bytes.fmk.service.leaderboard.Leaderboard;
import com.bytes.fmk.service.leaderboard.Score;
import com.bytes.fmk.service.leaderboard.ScoreCategory;
import com.bytes.fmk.service.leaderboard.ScoreboardType;

/**
 * The score board encapsulates the scoring logic for the leaderboard.
 * @author Kent
 */
public class Scoreboard {
	
	private static Logger logger = LoggerFactory.getLogger(Scoreboard.class);
	
	private ScoreboardType type;
	
	/**
	 * user  
	 * 	+ user: for each categories, add new score object for user
	 * 	- user: for each categories, remove score object belonging to user
	 * 
	 * score 
	 * 	- call update to replace ranked score list corresponding to category
	 * 
	 * keyed by CategoryName, 
	 * contains the list of user scores sorted by rank,
	 * Key =   CategoryName
	 * Value = List of ranked scores
	 */
	private Map<String, List<Score>> rankedScores;
	
		
	/** 
	 * This map is persisted.
	 * user
	 *  + user: create a new score with the user name for each category and add new entry
	 *  - user: remove entry
	 *  
	 * category
	 *  + category: for each user, add new map with new score
	 *  - category: for each user, remove map
	 *  
	 * score - get point object and update
	 * 
	 * This is the map of userId and its corresponding scores.
	 * The score collection is a map of scores keyed by category name.
	 * <pre>
	 * (userNameId, ((categoryName, points))
	 * </pre>
	 * Key = UserId
	 * Value = Map of ScoreCategory and its corresponding Score
	 * This map is used to create the leaderboard. 
	 **/
	private Map<String, Map<String, Score>> scoreEntries;
	
	
	/**
	 * This map is persisted.
	 * category
	 *  + category: add entry,   update userScore
	 *  - category: remove entry update userScore
	 *  
	 * score 
	 * 	- if new, add category
	 * Key = ScoreCategoryId, Value = ScoreCategory
	 * 
	 */
	private Map<String, ScoreCategory> categories;

	/**
	 * The title of the leaderboard
	 */
	private String title;
	
	
	/**
	 * Constructor. 
	 * Rank is not yet calculated for this leaderboard.
	 */
	public Scoreboard() {
		this(ScoreboardType.User, "Scoreboard");
	}
	
	/**
	 * Construct the leaderboard with a collection of scores.
	 * Leaderboard is set to be active for 1 day by default.
	 * Rank is not yet calculated for this leaderboard.
	 * @param type - the scoreboard type
	 * @param title  - the scoreboard title
	 */
	Scoreboard(ScoreboardType type, String title) {
		
		// Core
		this.type = type;
		this.title = title;
		this.scoreEntries = new ConcurrentHashMap<>();
		
		// Rank is not calculated
		this.rankedScores = new ConcurrentHashMap<>();
		
		// Default score category (Total)
		this.categories = new ConcurrentHashMap<>();
		addCategoryInternal(Leaderboard.TOTAL);
		
	}
	
	
	/**
	 * Retrieve the score and rank of the specified user
	 * @param userId - the id of the user
	 * @return the {@code Score} corresponding to the specified user
	 */
	public Score getScoreTotal(String userId) {
		return getScore(userId, Leaderboard.TOTAL);
	}

	
	/**
	 * Retrieve the score and rank of the specified user
	 * @param entryId - the id of the entry, can be userId, teamId, etc...
	 * @param category - the specific category
	 * @return the {@code Score} corresponding to the specified user
	 */
	public Score getScore(String entryId, String categoryName) {
		
		if (entryId == null) {
			logger.error("user id not provided");
			return null;
		}
		
		if (!scoreEntries.containsKey(entryId)) {
			logger.error("entry is not registered: " + entryId);
			return null;
		}
		
		Score score;
		if (!scoreEntries.get(entryId).containsKey(categoryName)) {
			logger.error("No score found for " + entryId + " category: " + categoryName);
			score = new Score(entryId, categoryName);
		} else {
			score = scoreEntries.get(entryId).get(categoryName);
		}
		return score;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public Map<String, Score> getScores(String entryId) {
		
		if(!scoreEntries.containsKey(entryId)) {
			logger.warn("Entry Id not found: " + entryId);
			return new HashMap<>();
		}
		return scoreEntries.get(entryId);
		
	}

	/**
	 * Get the rank based on total score
	 * @return the rank of the total
	 */
	int getRank(String userId) {
		return getRank(userId, Leaderboard.TOTAL);
	}

	
	/**
	 * Get the rank of the specified score
	 * @param userId - the user id
	 * @param categoryName - the specific category, identified by its name
	 * @return the rank of the specified score, -1 if user is not ranked
	 */
	int getRank(String userId, String categoryName) {
		
		ScoreCategory category = getCategory(categoryName);
		if (category.isStaled()) {
			logger.warn("Rank is staled. It might be incorrect.");
		}
		
		int rank = -1;
		try {
			Score score = getScore(userId, categoryName);
			rank = rankedScores.get(categoryName).indexOf(score) +1;
		} catch (Exception e) {
			logger.error("Unable to get user rank.", e);
		}
		return rank;
		
	}
	
	
	/**
	 * Add the specified category
	 * @param category - the categories to add
	 */
	public void addCategories(String... categoryNames) {
		
		for (String categoryName : categoryNames) {
			if (categories.containsKey(categoryName)) {
				logger.warn("Category: " + categoryName + " already exists, ignoring");
				continue;
			}
			
			if (categoryName.equalsIgnoreCase(Leaderboard.TOTAL)) {
				throw new IllegalArgumentException(categoryName + " is reserved");
			}
			
			addCategoryInternal(categoryName);
		}
	}

	
	/**
	 * Indicates if the category exists.
	 * @param categoryName - the name of the score category
	 * @return true if it exists
	 */
	public boolean hasCategory(String categoryName) {
		return categories.containsKey(categoryName);
	}
	
	
	/**
	 * Add a new category. This internal method bypass the 
	 * check to reserved categories.
	 * @param categoryName - the name of the category
	 * @return the {@code ScoreCategory}
	 */
	 ScoreCategory addCategoryInternal(String categoryName) {
		
		ScoreCategory category = new ScoreCategory(categoryName);
		categories.put(categoryName, category);
		
		// Add new category to all user scores
		for (String userId : scoreEntries.keySet()) {
			Map<String, Score> scoreByCategory = scoreEntries.get(userId);
			scoreByCategory.put(categoryName, new Score(userId, categoryName));
		}
		return category;
	}

	/**
	 * Get the ranked score corresponding to the category
	 * @param categoryName - the category
	 * @return the rankedScores
	 */
	List<Score> getRankedScores(String categoryName) {
		return rankedScores.get(categoryName);
	}
	
	/**
	 * Request to update the ranks for the specified category
	 * @param categoryName - the category name
	 * @param scores - the ranked scores
	 */
	void setRankedScores(String categoryName, ArrayList<Score> scores) {
		rankedScores.put(categoryName, scores);
	}

	public String getTitle() {
		return title;
	}

	void setTitle(String title) {
		this.title = title;
	}


	Map<String, Map<String, Score>> getScoreEntries() {
		return scoreEntries;
	}

	void setScoreEntries(Map<String, Map<String, Score>> scoreEntries) {
		this.scoreEntries = scoreEntries;
	}
	
	/**
	 * Retrieve the category registered to this leaderboard
	 * @param name - the name of the category
	 * @return the ScoreCategory, null if not found
	 */
	ScoreCategory getCategory(String name) {
		
		if (categories.containsKey(name)) {
			return categories.get(name);
		}
		
		logger.error(type + " category NOT found: " + name);
		return null;
	}
	
	Collection<ScoreCategory> getCategories() {
		return categories.values();
	}
	
	
	public Set<String> getCategoryNames() {
		return categories.keySet();
	}
 	
	
	/**
	 * Get the number of users participating in this leaderboard.
	 * @return the number of users in this leaderboard.
	 */
	public int getSize() {
		return scoreEntries.keySet().size();
	}

	public Set<String> getEntries() {
		return scoreEntries.keySet();
	}

	public ScoreboardType getType() {
		return type;
	}

	public void setType(ScoreboardType type) {
		this.type = type;
	}

}
