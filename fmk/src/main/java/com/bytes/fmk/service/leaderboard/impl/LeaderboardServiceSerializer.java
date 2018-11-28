package com.bytes.fmk.service.leaderboard.impl;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.bytes.fmk.service.persistence.Serializer;
import com.google.gson.reflect.TypeToken;

public class LeaderboardServiceSerializer extends Serializer {
	
	public static final String RESOURCE_ID = "LeaderboardServiceResourceID";
	private Map<String, Type> resourceIdMap;
	
	/**
	 * Default constructor.
	 */
	public LeaderboardServiceSerializer() {
		resourceIdMap = new HashMap<>();
		resourceIdMap.put(RESOURCE_ID, 
				new TypeToken<LeaderboardServiceImpl>(){}.getType());
	}
	
	/**
	 * Get the resourceId to type token mapping.
	 * @return The the mapping of TypeToken to resourceId
	 */
	public Map<String, Type> getResourceIdMap() {
		return resourceIdMap;
	}
}
