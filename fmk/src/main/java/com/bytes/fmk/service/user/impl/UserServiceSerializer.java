package com.bytes.fmk.service.user.impl;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.bytes.fmk.data.model.User;
import com.bytes.fmk.service.persistence.Serializer;
import com.google.gson.reflect.TypeToken;

public class UserServiceSerializer<T extends User> extends Serializer {

	public static final String RESOURCE_ID = "UserServiceResourceID";
	private Map<String, Type> resourceIdMap;
	
	/**
	 * Default constructor.
	 */
	public UserServiceSerializer() {
		resourceIdMap = new HashMap<>();
		resourceIdMap.put(RESOURCE_ID, 
				new TypeToken<UserServiceImpl<User>>(){}.getType());
	}

	/**
	 * Register a resource and its corresponding type. 
	 * @param resourceId
	 * @param type - the type use to deserialize the data
	 */
	public void registerResource(String resourceId, Type type) {
		resourceIdMap.put(resourceId, type);
	}
	
	
	/**
	 * Get the resourceId to type token mapping.
	 * @return The the mapping of TypeToken to resourceId
	 */
	public Map<String, Type> getResourceIdMap() {
		return resourceIdMap;
	}
}
