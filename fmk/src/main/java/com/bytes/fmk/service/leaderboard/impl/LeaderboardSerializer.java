package com.bytes.fmk.service.leaderboard.impl;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.bytes.fmk.service.persistence.Serializer;
import com.google.gson.reflect.TypeToken;

public class LeaderboardSerializer extends Serializer {

	private Map<String, Type> resourceIdMap;
	
	/**
	 * Default constructor.
	 */
	public LeaderboardSerializer() {
		resourceIdMap = new HashMap<>();
	}
	
	/**
	 * Register the leaderboard to be persisted.
	 * This allows the leaderboard to be loaded by the corresponding id.
	 * @param leaderboardId - the leaderboard id to be persisted and retrieved.
	 */
	public void registerLeaderboard(String leaderboardId) {
		resourceIdMap.put(leaderboardId, 
				new TypeToken<LeaderboardImpl>(){}.getType());
	}
	
	
	/**
	 * Get the resourceId to type token mapping.
	 * @return The the mapping of TypeToken to resourceId
	 */
	public Map<String, Type> getResourceIdMap() {
		return resourceIdMap;
	}
}
