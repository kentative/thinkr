package com.bytes.fmk.payload;

import java.util.Set;

/**
 * Created by Kent on 10/14/2016.
 */

public interface Payload<T> {
	
	// Change these to enums - these are overloaded in context
	final int STATUS_OK = 0;
	final int STATUS_ERROR = 1;
	
	// Change these to enums?
	final int POST   = 1;
	final int UPDATE = 2;
	final int DELETE = 3;
	final int GET	 = 4;
	final int NOTIFY = 5;
	
	int getStatus();
	
	void setStatus(int status);
	
	/**
	 * Request to add the specified destination id.
	 * 
	 * @param sessionId - the destination id
	 */
	void addDestination(String sessionId);

	/**
	 * @param destinationIds - the destinationId to add
	 */
	void addDestinations(Set<String> destinationIds);

	T getData();

	void setData(T data);

	String getId();

	void setId(String id);

	int getType();

	void setType(int type);

	String getDataType();
	
	void setDataType(String dataType);
	
	/**
	 * @return the sourceId
	 */
	String getSourceId();

	/**
	 * @param sourceId
	 *            the sourceId to set
	 */
	void setSourceId(String sourceId);

	/**
	 * @return the destinationId
	 */
	Set<String> getDestinationIds();
}
