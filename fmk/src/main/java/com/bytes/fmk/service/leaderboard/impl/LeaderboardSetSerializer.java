package com.bytes.fmk.service.leaderboard.impl;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.bytes.fmk.service.persistence.Serializer;
import com.google.gson.reflect.TypeToken;

public class LeaderboardSetSerializer extends Serializer {
	
	public static final String RESOURCE_ID = "LeaderboardSetResourceID";
	private Map<String, Type> resourceIdMap;
	
	/**
	 * Default constructor.
	 */
	public LeaderboardSetSerializer() {
		resourceIdMap = new HashMap<>();
		resourceIdMap.put(RESOURCE_ID, 
				new TypeToken<LeaderboardSetImpl>(){}.getType());
	}
	
	/**
	 * Get the resourceId to type token mapping.
	 * @return The the mapping of TypeToken to resourceId
	 */
	public Map<String, Type> getResourceIdMap() {
		return resourceIdMap;
	}
}
