package com.bytes.fmk.service.persistence;

import java.util.Map;

import com.bytes.fmk.service.ThinkrService;

public interface PersistenceService extends ThinkrService {
	
	
	/**
	 * Request to persist the specified data object with a key
	 * @param key - the key to retrieve the data
	 * @param data - the data to be persisted
	 * @return true if successful
	 */
	public <D> boolean saveData(String key, D data);
	
	
	/**
	 * Request to load the data corresponding to the specified key
	 * @param key - the key corresponding to the data
	 * @param serializer - contains the the key to token type mapping for JSON deserialization
	 * @return the data
	 */
	public <D> D loadData(String key, Serializer serializer);
	
	
	/**
	 * Request to save the map to redis
	 * @param resourceId - The redis map id
	 * @param map - The map containing data. The data will be converted to JSON string
	 * @return true if the serialization and persistence was successful
	 */
	<D> boolean saveMap(String resourceId, Map<String, D> map);

	
	/**
	 * Request to load the map from redis. 
	 * @param resourceId - the redis map id
	 * @param serializer - the serializer to convert the string data into strongly typed object
	 * @return The strongly typed map containing data specified by the resource id
	 * @see Serializer
	 */
	<D> Map<String, D> loadMap(String resourceId, Serializer serializer);
	
	
	/**
	 * Request to clear all data associated with the specified resource id
	 * @param resourceId - the resource id
	 * @return true if the one or more entries were deleted, false otherwise.
	 */
	public boolean clear(String resourceId);
	
}
