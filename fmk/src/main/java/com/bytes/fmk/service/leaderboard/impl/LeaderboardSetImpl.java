package com.bytes.fmk.service.leaderboard.impl;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bytes.fmk.service.Thinkr;
import com.bytes.fmk.service.leaderboard.Cycle;
import com.bytes.fmk.service.leaderboard.Leaderboard;
import com.bytes.fmk.service.leaderboard.LeaderboardSet;
import com.bytes.fmk.service.persistence.PersistenceMode;
import com.bytes.fmk.service.persistence.Serializer;

public class LeaderboardSetImpl implements LeaderboardSet {

	private static final String DT_PATTERN = "MM-dd-yyyy";
	static final String DELIMITER = "_";
	
	private static Logger logger = LoggerFactory.getLogger(LeaderboardSetImpl.class);
	
	/**
	 * The leaderboard map, keyed by cycle enumerations.
	 */
	private Map<Cycle, String> leaderboardIds;
	
	private transient Serializer serializer;
	
	/**
	 * The title of all the leaderboards in this set.
	 * The differentiation is in their cycle.
	 */
	private String title;
	
	
	/**
	 * The unique identifier
	 */
	private String id;

	public LeaderboardSetImpl() {
		this("Untitled");
	}
	
	/**
	 * Construct a LeaderboardSet with the specified title
	 * @param title - the title
	 */
	public LeaderboardSetImpl(String title) {
		this.leaderboardIds = new ConcurrentHashMap<>();
		this.title = title;
		this.id = title;
		this.serializer = new LeaderboardSetSerializer();
	}
	
	
	@Override
	public boolean add(Leaderboard...leaderboards) {
		for (Leaderboard leaderboard : leaderboards) {

			if (!(leaderboard instanceof LeaderboardImpl)) {
				logger.error("LeaderboardSet is only supported by LeaderboardImpl");
				return false;
			}
			
			if (title == null) {
				this.title = leaderboard.getTitle();
			} else {
				if (!title.equals(leaderboard.getTitle())) {
					logger.error("All leaderboards in a set has to have the same title.");
					return false;
				}
			}
			
			// Change leaderboard generated id to use set structured id
			Cycle cycle = leaderboard.getCycle();
			((LeaderboardImpl) leaderboard).setId(generateLeaderboardId(leaderboard));
			leaderboard.setGroupId(this.id);
			if (leaderboardIds.containsKey(cycle)) {
				logger.info(String.format(
						"Replacing %1$s leaderboard from: \n  %2$s to \n  %3$s ",
						cycle, leaderboardIds.get(cycle), leaderboard.getId()));
			}
			leaderboardIds.put(cycle, leaderboard.getId());
		}
		
		return true;
	}
	
	
	@Override
	public String getLeaderboard(Cycle cycle) {
		
		if (leaderboardIds.containsKey(cycle)) {
			return leaderboardIds.get(cycle);
		}
		return null;
	}

	
	/**
	 * Get the leaderboard id based on a structured format:
	 * title + cycle + day leaderboard of start time in ISO_DATE format
	 * @param leaderboard - the leaderboard
	 * @return the structured id - can be use to lookup in persistence store
	 */
	@Override
	public String generateLeaderboardId(Leaderboard leaderboard) {
		return generateLeaderboardId(leaderboard.getCycle(), leaderboard.getStartDate());
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String generateLeaderboardId(Cycle cycle, OffsetDateTime date) {
		return title + DELIMITER +
				cycle + DELIMITER +
				date.format(DateTimeFormatter.ofPattern(DT_PATTERN));
		
	}
	
	
	@Override
	public Collection<String> getAll() {
		return leaderboardIds.values();
	}

	
	@Override
	public boolean save(PersistenceMode mode, String resourceID) {
		return Thinkr.INSTANCE.getPersistenceService(mode)
				.saveData(resourceID, this);
	}

	
	@Override
	public LeaderboardSet load(PersistenceMode mode, String resourceID) {
		
		try {
			LeaderboardSetImpl loaded = Thinkr.INSTANCE.getPersistenceService(mode)
					.loadData(resourceID, getSerializer());
			
			this.id = loaded.id;
			this.leaderboardIds = loaded.leaderboardIds;
			this.title = loaded.title;
			
		} catch (Exception e) {
			logger.error("Unable to load LeaderboardSet data: " + e.getMessage());
		}
		return this;
	}

	
	@Override
	public String getId() {
		return id;
	}

	
	@Override
	public Serializer getSerializer() {
		return serializer;
	}
}
