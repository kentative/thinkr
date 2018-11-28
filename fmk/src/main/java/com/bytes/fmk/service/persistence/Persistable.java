package com.bytes.fmk.service.persistence;

/**
 * TBD
 *
 * @param <T>
 */
public interface Persistable<T>{

	/**
	 * Request to save the service states to an external cache
	 * @param mode - the persistence mode
	 * @param resourceID - the resource identifier to use when loading this data
	 * @return true if successfully persisted
	 */
	public boolean save(PersistenceMode mode, String resourceID);
	
	
	/**
	 * Request to load the {@code Persistable} states from an external cache
	 * @param mode - the persistence mode
	 * @param resourceID - the resource identifier to retrieve the data
	 * @return the loaded {@code Persistable}. If failed, returns null
	 */
	public T load(PersistenceMode mode, String resourceID);
	
	
	/**
	 * Get the serializer that support loading operation
	 * @return the serializer to be used for loading the data from the external cache
	 */
	public Serializer getSerializer();

	
}
