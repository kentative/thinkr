package com.bytes.fmk.service.leaderboard.impl;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bytes.fmk.data.model.Team;
import com.bytes.fmk.data.model.User;
import com.bytes.fmk.service.Thinkr;
import com.bytes.fmk.service.leaderboard.Cycle;
import com.bytes.fmk.service.leaderboard.Leaderboard;
import com.bytes.fmk.service.leaderboard.LeaderboardService;
import com.bytes.fmk.service.leaderboard.LeaderboardSet;
import com.bytes.fmk.service.leaderboard.Score;
import com.bytes.fmk.service.leaderboard.ScoreCategory;
import com.bytes.fmk.service.leaderboard.ScoreboardType;
import com.bytes.fmk.service.leaderboard.impl.util.LeaderboardUtil;
import com.bytes.fmk.service.leaderboard.ledger.Ledger;
import com.bytes.fmk.service.leaderboard.ledger.LedgerEntry;
import com.bytes.fmk.service.leaderboard.ledger.Recordable;
import com.bytes.fmk.service.leaderboard.ledger.impl.LedgerImpl;
import com.bytes.fmk.service.leaderboard.ledger.impl.LedgerSerializer;
import com.bytes.fmk.service.leaderboard.report.Report;
import com.bytes.fmk.service.leaderboard.report.ReportBuilder;
import com.bytes.fmk.service.persistence.PersistenceMode;
import com.bytes.fmk.service.persistence.Serializer;
import com.bytes.fmk.service.persistence.impl.RedisImpl;

/**
 * @author Kent
 *
 */
public class LeaderboardServiceImpl implements LeaderboardService {
	
	/** Logger for this class */
	private static Logger logger = LoggerFactory.getLogger(LeaderboardServiceImpl.class);
	
	/**
	 * The serializer for the all leaderboards.
	 */
	private transient LeaderboardSerializer leaderboardSerializer;
	
	
	/**
	 * The serializer for this class, the leaderboard service
	 */
	private transient LeaderboardServiceSerializer serializer;
	
	
	/** 
	 * The registered leaderboards
	 * key = leaderboard id
	 * value = the {@code Leaderboard} instance 
	 */
	private Map<String, LeaderboardImpl> leaderboards;
	
	
	/**
	 * The registered leaderboard sets
	 * key = leaderboard id
	 * value = the {@code LeaderboardSet} instance
	 */
	private Map<String, LeaderboardSetImpl> leaderboardSets;
	
	
	/**
	 * The leaderboard ledger to store all {@code Recordable}.
	 */
	private LedgerImpl ledger;
	
	
	/**
	 * Default constructor
	 */
	public LeaderboardServiceImpl() {
		this.leaderboardSerializer = new LeaderboardSerializer();
		this.serializer = new LeaderboardServiceSerializer();
		this.leaderboards = new ConcurrentHashMap<>();
		this.ledger = new LedgerImpl();
		this.leaderboardSets = new ConcurrentHashMap<>();
	}
	

	@Override
	public LeaderboardSet createSet(String title, Cycle...cycles) {
		
		LeaderboardSetImpl set = new LeaderboardSetImpl(title);
		for (Cycle cycle : cycles) {
			LeaderboardImpl leaderboard = new LeaderboardImpl(title, cycle);
			set.add(leaderboard);
			register(leaderboard);
		}
		
		leaderboardSets.put(set.getId(), set);
		return set; 
	}
	
	
	@Override
	public LeaderboardImpl create(String title, Cycle cycle) {
		LeaderboardImpl leaderboard = new LeaderboardImpl(title, cycle);
		register(leaderboard);
		return leaderboard; 
	}
	
	
	/** {@inheritDoc} */
	public List<Score> listDescending(String leaderboardId, int start, int count) {
		return listDescending(leaderboardId, ScoreboardType.User, LeaderboardImpl.TOTAL, start, count);
	}
	
	

	/** {@inheritDoc} */
	public List<Score> listDescending(String leaderboardId, ScoreboardType type, String categoryName, int start, int count) {
		
		logger.debug(String.format(
				"List descending: %1$s scoreboard: %2$s category: %3$s entry count: %4$d", 
				leaderboardId, type, categoryName, count));
		
		LeaderboardImpl leaderboard = getLeaderboard(leaderboardId);
		if (!validateRange(leaderboard, type, categoryName, start, count)) {
			return new ArrayList<Score>(0);
		}
		
		List<Score> rankedScores = leaderboard.getRankedScores(categoryName, type);

		// Adjust for 0-based index
		start = (start >= rankedScores.size()) ? rankedScores.size()-1:  start-1; 
		
		// Bound the fromIndex 
		int fromIndex = (start < 0) ?0 :start;
		
		// Bound the toIndex
		int toIndex = start + count;
		int padding = 0;
		if (toIndex < 0)  {
			toIndex = 0;
		} else if (toIndex >= rankedScores.size()) {
			toIndex = rankedScores.size()-1;
			padding = 1;
		}
		return rankedScores.subList(fromIndex, toIndex + padding);
	}
	
	

	/** {@inheritDoc} */
	public List<Score> listAscending(String leaderboardId, ScoreboardType type, String categoryName, int rank, int count) {
		
		LeaderboardImpl leaderboard = getLeaderboard(leaderboardId);
		if (!validateRange(leaderboard, type, categoryName, rank, count)) {
			return new ArrayList<Score>(0);
		}
		
		List<Score> rankedScores = leaderboard.getRankedScores(categoryName, type);
		int maxRank = rankedScores.size();
		
		// Bound rank and count value (can't be more than the size)
		if (rank  > maxRank) rank = maxRank;
		if (count > maxRank) count = maxRank;
		
		// Bound the fromIndex --inclusive, 0-based
		// Adjust for 0-based index
		int fromIndex = rank - count;
		if (fromIndex < 0)  {
			count += fromIndex; // adjust the count
			fromIndex = 0;
		} 
		
		// Bound the toIndex --exclusive
		int toIndex = fromIndex + count;
		return rankedScores.subList(fromIndex, toIndex);
	}
	

	/** {@inheritDoc} */
	public List<Score> listRanks(String leaderboardId, ScoreboardType type, String categoryName, String userId, int count) {
		
		LeaderboardImpl leaderboard = getLeaderboard(leaderboardId);
		int rank = getRank(leaderboardId, categoryName, userId) -1;
		if (!validateRange(leaderboard, type, categoryName, rank, count)) {
			return new ArrayList<Score>(0);
		}
		
		List<Score> rankedScores = leaderboard.getRankedScores(categoryName, type);
		int maxRank = rankedScores.size();
		
		// Bound rank and count value (can't be more than the size)
		if (rank  > maxRank) rank = maxRank;
		if (count > maxRank) count = maxRank;
		
		// Bound the fromIndex --inclusive, 0-based
		// Adjust for 0-based index
		int fromIndex = rank - count;
		if (fromIndex < 0)  {
			count += fromIndex; // adjust the count
			fromIndex = 0;
		} 
		
		// Bound the toIndex --exclusive
		int toIndex = rank + count;
		if (toIndex > maxRank)  {
			toIndex = maxRank;
		} 
		return rankedScores.subList(fromIndex, toIndex);
	}
	
	
	/**
	 * Check range to make sure: 
	 * <li>start index is not 0 or negative,
	 * <li>count is not 0 or negative, 
	 * <li>the leaderboard is not empty
	 * @param leaderboard - the leaderboard
	 * @param type - the scoreboard type
	 * @param categoryName - the category name
	 * @param start - the start rank 
	 * @param count - the number of entries
	 * @return true if the parameters are valid
	 */
	private boolean validateRange(LeaderboardImpl leaderboard, ScoreboardType type, String categoryName, int start, int count) {

		if (start < 1) {
			logger.warn("Invalid parameter specified rank is out of range");
			return false;
		}
		
		if (count < 1) {
			logger.warn("Invalid parameter count is less than 1: " + count);
			return false;
		}

		Map<String, Map<String, Score>> userScores = leaderboard.getUserScores();
		if (userScores== null || userScores.isEmpty()) {
			logger.warn("Leaderboard (userScores) is empty. Add some users!");
			return false;
		}
		
		List<Score> rankedScores = leaderboard.getRankedScores(categoryName, type);
		if (rankedScores== null || rankedScores.isEmpty()) {
			logger.warn("Leaderboard (rankedScores) is empty. Run calculation first!");
			return false;
		}
		
		return true;
	}


	/**
	 * {@inheritDoc}
	 */
	public void calculate(String leaderboardId) {
		LeaderboardImpl leaderboard = getLeaderboard(leaderboardId);
		for(ScoreCategory category : leaderboard.getCategories()) {
			calculate(leaderboardId, category.getName());
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void calculate(String leaderboardId, String categoryName) {
		
		LeaderboardImpl leaderboard = getLeaderboard(leaderboardId);
		for (Scoreboard scoreboard : leaderboard.getScoreboards().values()) {
			calculateScoreboard(leaderboardId, scoreboard, categoryName);
		}
	}
	private void calculateScoreboard(String leaderboardId, Scoreboard scoreboard, String categoryName) {
		
		ScoreCategory category = scoreboard.getCategory(categoryName);
		if (!category.isStaled()) {
			logger.trace("Category " + categoryName + " is up-to-date, recalculation is not needed.");
			return; 
		}

		logger.trace("calculating " + categoryName + " rank");
		
		Map<String, Map<String, Score>> userScores = scoreboard.getScoreEntries();
		Set<String> userIds = userScores.keySet();

		int i = 0;
		Score[] unsortedScores = new Score[userIds.size()];
		for (String userId : userScores.keySet()) {
			unsortedScores[i++] = scoreboard.getScore(userId, categoryName);
		};
		
		// Sort using ScoreCategory comparator --effectively ranking the scores
		Arrays.sort(unsortedScores, category);
		
		// Update leaderboard with sorted ranks
		scoreboard.setRankedScores(categoryName, new ArrayList<>(Arrays.asList(unsortedScores)));
		category.setCalculatedTime(OffsetDateTime.now());
		category.setStaled(false);

		// Update the total
		logger.trace("Updating total...");
		calculate(leaderboardId, Leaderboard.TOTAL);
	}

	 
	/**
	 * Request to update the leaderboard with the list of ledger entries
	 * @param id - leaderboard id
	 * @param recordEntries - list of ledger entries grouped by user id
	 */
	public boolean update(String id, Map<String, List<LedgerEntry>> recordEntries) {
		
		if (recordEntries == null || recordEntries.isEmpty()) {
			logger.error("Ledger entry is null or empty");
			return false;
		}
		
		boolean result = true;
		for (String userId : recordEntries.keySet()) {
			User user = Thinkr.INSTANCE.getUserService().getUser(userId);
			if (user == null) {
				throw new IllegalStateException("User " + userId + " is not registered with UserService");
			}
			
			addUser(id, user);
			for (LedgerEntry entry : recordEntries.get(userId)) {
				result &= update(id, userId, entry);
			}
		}
		return result;
	}
	
	
	/**
	 * Request to update the leaderboard with a {@code Recordable}
	 * @param id - the leaderboard or the leaderboard set id
	 * @param userId - the user id
	 * @param recordable - the {@code Rewardable} action
	 * @return true if successful
	 */
	public boolean update(String id, String userId, Recordable recordable) {
		
		LeaderboardImpl leaderboard = leaderboards.get(id);
		String groupId = (leaderboard != null) ?leaderboard.getGroupId() :id;
		if (groupId != null && leaderboardSets.containsKey(groupId)) {
			for (String leaderboardId : leaderboardSets.get(groupId).getAll()) {
				update(getLeaderboard(leaderboardId), userId, recordable);
			}
			return true;
		}
		return update(leaderboard, userId, recordable);
	}
	
	
	/**
	 * Request to update the leaderboard with a {@code Recordable}
	 * Leaderboard is only update if it is active during the time of the recordable.
	 * If the recordable time is null, it is defaulted to now.
	 * 
	 * @param leaderboard - the leaderboard
	 * @param userId - the user id
	 * @param recordable - the {@code Rewardable} action
	 * @return true if successful
	 */
	private boolean update(LeaderboardImpl leaderboard, String userId, Recordable recordable) {
		
		if (leaderboard == null) {
			logger.error("Leaderboard is not registered");
			return false;
		}
		
		logger.trace(String.format(
				"Update recordable for %1$s in leaderboard: %2$s",  
				userId, leaderboard.getId()));
		
		OffsetDateTime time = recordable.getTime();
		if (time == null) {
			time = OffsetDateTime.now();
		}
		
		if (logger.isDebugEnabled()) {
			logger.trace(String.format("\n '%5$s' Leaderboard '%1$s' updated at '%2$s'. Start: %3$s End: %4$s",
					leaderboard.getTitle(), time.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME), 
					leaderboard.getStartDate().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME), 
					leaderboard.getEndDate().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
					leaderboard.getCycle()));
		}
		
		if (!leaderboard.isActive(time)) {
			if (leaderboard.getGroupId() != null && leaderboard.isAutoRenew()) {
				logger.debug("\n\nLeaderboard is part of a group and auto renew is set. Generating a replacement of type: " + leaderboard.getCycle());
				LeaderboardSet set = leaderboardSets.get(leaderboard.getGroupId());
				LeaderboardImpl replacement = new LeaderboardImpl(leaderboard.getTitle(), leaderboard.getCycle());
				replacement.setAutoRenew(true);
				Thinkr.INSTANCE.getUserService().getUsers().forEach(user -> addUser(replacement, user));
				replacement.setStartAndEndTime(time);
				set.add(replacement);
				register(replacement);
				leaderboard.setGroupId(null);
				leaderboard = replacement;
				logger.debug("Replacment generation completed for type : " + leaderboard.getCycle() +"\n\n");
			} else {
				logger.debug("Leaderboard is inactive.");
				return false;
			}
		}
		
		// Check category
		if (!leaderboard.hasCategory(recordable.getCategoryName())) {
			leaderboard.addCategories(recordable.getCategoryName());
		}
		
		// Update all scoreboards
		User user = Thinkr.INSTANCE.getUserService().getUser(userId);
		if (user == null) throw new IllegalStateException("User " + userId + " is not registered with UserService");
		for (Scoreboard scoreboard :leaderboard.getScoreboards().values()) {
			updateScoreboard(leaderboard, scoreboard, user, recordable);
		}
		
		// Update the ledger
		logRewardable(userId, recordable);
		
		// Persist on update if auto update is set
		if (leaderboard.isAutoPersist()) {
			save(leaderboard, PersistenceMode.RedisAzure);
		}
		return true;
	}
	
		 
	/**
	 * Request to update the scoreboard with a {@code Recordable}
	 * 
	 * @param leaderboard
	 * @param scoreboard
	 * @param user - the user
	 * @param recordable - the {@code Rewardable} action
	 * @return true if successful
	 */
	private boolean updateScoreboard(
			Leaderboard leaderboard, 
			Scoreboard scoreboard, 
			User user, 
			Recordable recordable) {
		
		logger.debug(String.format(
				"Updating %1$s scoreboard for %2$s - category: %3$s, points: %4$d",
				scoreboard.getType(), 
				user.getDisplayName(),
				recordable.getCategoryName(), 
				recordable.getPoints()));
		
		String categoryName = recordable.getCategoryName();
		int points = recordable.getPoints();
		String id = getScoreboardId(user, scoreboard.getType());
		
		if (id == null) {
			logger.warn("No valid " + scoreboard.getType() + " scoreboard entry found for user: " + user.getDisplayName());
			return false;
		}
		
		logger.trace(String.format("Id='%1$s' User='%2$s'", id, user.getDisplayName()));
		
		Score score = scoreboard.getScore(id, categoryName);
		if (score == null) {
			logger.warn("Score data not found for \n" +
					" leaderboardId:   " + leaderboard.getId() + "\n" + 
					" scoreboard type: " + scoreboard.getType() + "\n" + 
					" user:            " + user.getDisplayName() + "\n" + 
					" score category:  " + categoryName
					);
			
			return false;
		}
		
		score.add(points);
		scoreboard.getCategory(categoryName).setStaled(true);
		
		// Update Total
		scoreboard.getScoreTotal(id).add(points);
		scoreboard.getCategory(LeaderboardImpl.TOTAL).setStaled(true);
		
		if (leaderboard.isAutoUpdate()) {
			logger.trace("Auto updating rank for leaderboard: " + leaderboard.getTitle());
			calculate(leaderboard.getId(), categoryName);
		}
		return true;
	}
	
	
	/**
	 * Get the user scoreboard id corresponding to the scoreboard type
	 * @param user - the user reporting the score, not null
	 * @param type - the scoreboard type
	 * @return the score id
	 */
	private String getScoreboardId(User user, ScoreboardType type) {
		
		String id;
		switch (type) {
		case User:
			return user.getId();

		case Team:
			id = user.getTeam().getName();
			if (id.equals(Team.UNDEFINED)) return null;
			return id;
			
		case Guild:
			id = user.getTeam().getGuild().getName();
			if (id.equals(Team.UNDEFINED)) return null;
			return id;
			
		case League:
			throw new RuntimeException("League is not yet implemented");
			
		default:
			return user.getId();
		}
	}
	
	
	/**
	 * Request to log a recordable action
	 * @param userId - the userId
	 * @param recordable - the recordable to log
	 */
	private void logRewardable(String userId, Recordable recordable) {
		ledger.add(userId, recordable);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public boolean addUser(String id, User user) {
		
		String userId = user.getId();
		logger.debug("Adding user: " + user.getDisplayName() + "/" + userId);
		
		if (leaderboardSets.containsKey(id)) {
			LeaderboardSet set = leaderboardSets.get(id);
			set.getAll().parallelStream()
				.map(this::getLeaderboard)
				.forEach(x -> addUser(x, user));
			return true;
		}
		return addUser(getLeaderboard(id), user);
	}
	
	
	/**
	 * Request to register the user in the leaderboard
	 * @param id - the leaderboard or leaderboardSet id
	 * @param user - the user
	 * @return true if successful
	 */
	private boolean addUser(Leaderboard leaderboard, User user) {

		// Update all scoreboards
		for (Scoreboard scoreboard : leaderboard.getScoreboards().values()) {
			registerScoreboardEntry(scoreboard, user);
		}
		
		return Thinkr.INSTANCE.getUserService().registerUser(user.getId(), user);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	private boolean registerScoreboardEntry(Scoreboard scoreboard, User user) {
		
		String entryId = getScoreboardId(user, scoreboard.getType());
		logger.trace(String.format(
				"Adding Scoreboard Entry    - Type:%1$s, Entry:%2$s, User:%3$s",
				scoreboard.getType(), entryId, user.getDisplayName()));
		
		if (entryId == null) {
			logger.warn("No valid " + scoreboard.getType() + " scoreboard entry found for user: " + user.getDisplayName());
			return false;
		}
		
		Map<String, Map<String, Score>> userScores = scoreboard.getScoreEntries();
		if (userScores.containsKey(entryId)) {
			logger.trace("Entry is already added: " + entryId);
			return true;
		}
		
		// Add score category for new entry
		Map<String, Score> scores = new HashMap<>();
		for (ScoreCategory category : scoreboard.getCategories()) {
			logger.trace(String.format(
					"Adding Scoreboard Category - Type:%1$s, Category:%2$s, Entry:%3$s",
					scoreboard.getType(), category.getName(), entryId));
			
			scores.put(category.getName(), new Score(entryId, category.getName()));
			userScores.put(entryId, scores);
		}
		
		return true;
	}
	
	
	/**
	 * Request to unregister the user from the leaderboard
	 * TODO - not tested
	 * @param leaderboard - the leaderboard
	 * @param userId
	 * @param score
	 * @return
	 */
	public boolean removeUser(String leaderboardId, String userId) {
		LeaderboardImpl leaderboard = getLeaderboard(leaderboardId);
		Map<String, Map<String, Score>> userScores = leaderboard.getUserScores();
		if (userScores.containsKey(userId)) {
			Map<String, Score> scores = userScores.remove(userId);
			
			for (ScoreboardType scoreboardType : leaderboard.getScoreboards().keySet()) {
				for(String categoryName : scores.keySet()) {
					Score score = scores.remove(categoryName);
					leaderboard.getRankedScores(categoryName, scoreboardType).remove(score);
				}
			}
			return true;
		}
		return false;
	}
	
	
	/**
	 * Request to save leaderboard states to external cache
	 * @param leaderboard - the leaderboard
	 * @return true if successfully persisted
	 * @TODO use Persistable interface for leaderboard
	 */
	public boolean save(Leaderboard leaderboard, PersistenceMode mode) {
		
		RedisImpl datastore = RedisImpl.AzureThinkr;
		switch (mode) {
		case RedisAzure:
			logger.info("Saving to Redis Azure");
			datastore = RedisImpl.AzureThinkr;
			break;
			
		case RedisLocal:
			logger.info("Saving to Redis Local");
			datastore = RedisImpl.Local;
			break;
			
		default:
			logger.info("Saving to Redis Local");
			datastore = RedisImpl.Local;
		}
		
		boolean result = 
				datastore.saveData(leaderboard.getId(), leaderboard) && 
				ledger.save(mode, LedgerSerializer.RESOURCE_ID);
		
		if (result) {
			logger.debug("Successfully persisted, registering ids for retrieval: " + leaderboard.getId());
			leaderboardSerializer.registerLeaderboard(leaderboard.getId());
		}
		
		return result; 
	}

	
	/**
	 * {@inheritDoc}
	 * @TODO use Persistable interface for leaderboard - remove this
	 */
	public LeaderboardImpl load(String leaderboardId, PersistenceMode mode) {
		
		RedisImpl datastore = RedisImpl.AzureThinkr;
		try {
			switch (mode) {
			case RedisAzure:
				logger.info("Loading from Redis Azure");
				datastore = RedisImpl.AzureThinkr;
				break;
				
			case RedisLocal:
				logger.info("Loading from Redis Local");
				datastore = RedisImpl.Local;
				break;
				
			default:
				logger.info("Loading from Redis Local");
				datastore = RedisImpl.Local;
				break;
			}
		} catch (Exception e) {
			logger.error("Unable to load from external cache: " + leaderboardId, e);
			return null;
		}
		
		// Load leaderboard
		leaderboardSerializer.registerLeaderboard(leaderboardId);
		LeaderboardImpl leaderboard = datastore.loadData(leaderboardId, leaderboardSerializer);
		if (leaderboard == null) {
			logger.warn("No data found for leaderboard: " + leaderboardId);
			return null;
		}
		
		register(leaderboard);
		
		// Perform calculation to populate the ranks
		leaderboard.getCategories().forEach(category -> category.setStaled(true));
		calculate(leaderboardId);
		logger.info("Successfully loaded leaderboard: " + leaderboardId);
		return leaderboard;
	}
	
	
	/**
	 * Request to merge the user score maps from the leaderboard. 
	 * @param sourceId - the id of the source leaderboard
	 * @param sourceMode - the {@code PersistenceMode} of the source leaderboard
	 * @param targetId - the id of the target leaderboard 
	 * @param targetMode - the {@code PersistenceMode} of the target leaderboard
	 * @return the target leaderboard containing the data of both leaderboards
	 */
	public LeaderboardImpl merge(String sourceId, PersistenceMode sourceMode, String targetId, PersistenceMode targetMode) {

		logger.debug(String.format(
				"Merging %1$s(%2$s) to %3$s(%4$s)", 
				sourceId, sourceMode, targetId, targetMode));
		
		LeaderboardImpl source = load(sourceId, sourceMode);
		LeaderboardImpl target = load(targetId, targetMode);

		Map<String, Map<String, Score>> sourceUserScores = source.getUserScores();
		Map<String, Map<String, Score>> targetUserScores = target.getUserScores();
		
		// Merge source user's scores map to target
		for(String userId : sourceUserScores.keySet()) {
			if (targetUserScores.containsKey(userId)) {
				logger.debug("Merging user to target: " + userId);
				mergeScoreMap(sourceUserScores.get(userId), targetUserScores.get(userId), target);
			} else {
				logger.debug("Adding user to target: " + userId);
				targetUserScores.put(userId, sourceUserScores.get(userId));
			}
		}
		
		return target;
	}
	
	
	/**
	 * Request to merge the map containing {@code ScoreCategory} name as the key and the {@code Score} as the value.
	 * If the category matches, the scores will be added together. The results will be store to the target.
	 *  
	 * @param source - the source
	 * @param target - the target
	 * @param targetLeaderboard - the target leaderboard to update the category list
	 * @return the target map with data from the source
	 */
	private Map<String, Score> mergeScoreMap(Map<String, Score> source, Map<String, Score> target, LeaderboardImpl targetLeaderboard) {
		
		// Merge source scores to target
		for(String categoryName : source.keySet()) {
			// Handle matching entries
			if (target.containsKey(categoryName)) {
				logger.debug("Merging score category: " + categoryName);
				target.get(categoryName).add(source.get(categoryName).getPoints());
			} else {
				logger.debug("Adding new score category: " + categoryName);
				target.put(categoryName, source.get(categoryName));
				targetLeaderboard.addCategories(categoryName);
			}
		}
		
		return target;
	}


	/**
	 * {@inheritDoc}
	 */
	public int getRank(String leaderboardId, String categoryName, String userId) {
		LeaderboardImpl leaderboard = getLeaderboard(leaderboardId);
		return leaderboard.getRank(userId, categoryName);
	}

	
	String register(LeaderboardImpl leaderboard) {
		
		String id = leaderboard.getId();
		logger.debug("Registering leaderboard: " + id + " cycle: " + leaderboard.getCycle());
		
		if (leaderboards.containsKey(id)) {
			logger.warn("Leaderboard is already registered, overriding: " + id);
		}
		leaderboards.put(id, leaderboard);
		leaderboardSerializer.registerLeaderboard(id);		
		return id;
	}

	
	@Override
	public LeaderboardImpl getLeaderboard(String id) {
		
		if (id == null && leaderboards.size() == 1) {
			logger.debug("Retrieving the default leaderboard.");
			return leaderboards.values().iterator().next();
		}
		
		logger.trace("Retrieving leaderboard: " + id);
		if (leaderboards.containsKey(id)) {
			return leaderboards.get(id);
		} else {
			logger.warn("Leaderboard is not registered: " + id);
			return null;
		}		
	}

	
	@Override
	public Report generateReport(String leaderboardId, ReportBuilder builder) {
		
		if (builder == null) { 
			throw new IllegalArgumentException("ReportBuilder is null");
		}
		return builder.build(getLeaderboard(leaderboardId));
		
	}


	/**
	 * Get the ledger
	 * @return the ledger
	 */
	public Ledger getLedger() {
		return ledger;
	}


	@Override
	public boolean save(PersistenceMode mode, String resourceId) {
		return Thinkr.INSTANCE.getPersistenceService(mode).saveData(resourceId, this);
	}


	@Override
	public LeaderboardService load(PersistenceMode mode, String resourceId) {

		try {
			LeaderboardServiceImpl loadedInstance = 
					Thinkr.INSTANCE.getPersistenceService(mode).loadData(resourceId, serializer);

			// Restore all persisted data
			this.leaderboards = loadedInstance.leaderboards;
			this.leaderboardSets = loadedInstance.leaderboardSets;
			this.ledger = loadedInstance.ledger;
			
		} catch (Exception e) {
			logger.error("Unable to load leaderboard service data: " + e.getMessage(), e);
		}
		return this;
	}
	
	
	/**
	 * Retrieve the {@code LeaderboardSet} corresponding to the specified id
	 * @param id - the leaderboard set id
	 * @return the leaderboard set if found, null otherwise
	 */
	public LeaderboardSet getLeaderboardSet(String id) {
		
		logger.trace("Retrieving leaderboard set: " + id);
		if (leaderboardSets.containsKey(id)) {
			return leaderboardSets.get(id);
		} else {
			logger.warn("Leaderboard set is not registered: " + id);
			return null;
		}		 
	}


	@Override
	public Serializer getSerializer() {
		return serializer;
	}


	/** {@inheritDoc} */
	@Override
	public Leaderboard getLeaderboard(String id, Cycle cycle, OffsetDateTime time) {
		LeaderboardSetImpl set = leaderboardSets.get(id);
		if (set == null) {
			throw new IllegalStateException("Leaderboard Set is not found: " + id);
		}
		
		time = LeaderboardUtil.getStartTime(cycle, time);
		String leaderboardId = set.generateLeaderboardId(cycle, time);
		logger.debug(String.format(
				"Generated leaderboard id '%1$s' from set '%2$s'",
				leaderboardId, set.getId()));
		return leaderboards.get(leaderboardId);
	}
}
