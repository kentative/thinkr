package com.bytes.fmk.service.persistence;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.bytes.fmk.data.model.User;
import com.google.gson.reflect.TypeToken;

import redis.clients.jedis.Protocol.Command;

public class DefaultSerializer extends Serializer {

	private Map<String, Type> resourceIdMap;
	
	/**
	 * Default constructor.
	 */
	public DefaultSerializer() {
		resourceIdMap = new HashMap<>();
		resourceIdMap.put("Command", new TypeToken<Command>(){}.getType());
		resourceIdMap.put("User", new TypeToken<User>(){}.getType());
	}
	
	public void addMapping(String key, Type type) {
		resourceIdMap.put(key, type);
	}
	
	/**
	 * Get the resourceId to type token mapping.
	 * @return The the mapping of TypeToken to resourceId
	 */
	public Map<String, Type> getResourceIdMap() {
		return resourceIdMap;
	}

}
