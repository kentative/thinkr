package com.bytes.fmk.service.leaderboard.ledger.impl;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.bytes.fmk.service.persistence.Serializer;
import com.google.gson.reflect.TypeToken;

public class LedgerSerializer extends Serializer {

private Map<String, Type> resourceIdMap;
	
	public static final String RESOURCE_ID = "LeaderboardLedgerResourceID";

	/**
	 * Default constructor.
	 */
	public LedgerSerializer() {
		resourceIdMap = new HashMap<>();
		resourceIdMap.put(RESOURCE_ID, 
				new TypeToken<LedgerImpl>(){}.getType());
	}
	
	/**
	 * Get the resourceId to type token mapping.
	 * @return The the mapping of TypeToken to resourceId
	 */
	public Map<String, Type> getResourceIdMap() {
		return resourceIdMap;
	}

}
