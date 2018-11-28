package com.bytes.fmk.service.persistence;

import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bytes.fmk.service.persistence.impl.OffsetDateTimeDeserializer;
import com.bytes.fmk.service.persistence.impl.OffsetDateTimeSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public abstract class Serializer {
	
	protected static Logger logger = LoggerFactory.getLogger(Serializer.class);
	protected static Gson gson;
	
	static {
		gson = new GsonBuilder()
		        .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeSerializer())
		        .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeDeserializer())
		        .create();
	}
	
	
	/**
	 * The resource id to type token map used to deserialize data
	 * key = resourceId - The case-sensitive resourceId
	 * value = type token - The {@code TypeToken} 
	 * @see DefaultSerializer
	 */
	public abstract Map<String, Type> getResourceIdMap();
	
		
	/**
	 * Retrieve the strongly typed objects from the json-formatted-data
	 * 
	 * @param data - the data in json format, except when data type is String
	 * @param type - the data type
	 * @return the strongly typed object
	 */
	public <D> D getData(String data, String resourceId) {
		Type type = getResourceIdMap().get(resourceId);
		try {
			return gson.fromJson(data, type);
		} catch (Exception e) {
			logger.error("Unknown data type. Unable to convert to data object: " + resourceId + " " + type, e);
		}
		return null;
	}
	
	
	/**
	 * Retrieve the strongly typed objects from the json-formatted-data
	 * 
	 * @param data - the data in json format, except when data type is String
	 * @param type - the data type
	 * @return the strongly typed object
	 */
	public static <D> D fromJson(String data, String resourceId, Serializer serializer) {
		Type type = serializer.getResourceIdMap().get(resourceId);
		try {
			return gson.fromJson(data, type);
		} catch (Exception e) {
			logger.error("Unknown data type. Unable to convert to data object: " + resourceId + " " + type, e);
		}
		return null;
	}
	
	
	/**
	 * Returns the json representation of an object
	 * @param data - the object to serialize
	 * @return the json value
	 */
	public static String toJson(Object data) {
		return gson.toJson(data);
	}


	public void registerResourceID(String resourceId, Type type) {
		getResourceIdMap().put(resourceId, type);
	}

}
