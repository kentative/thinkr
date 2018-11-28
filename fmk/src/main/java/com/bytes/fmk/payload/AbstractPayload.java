package com.bytes.fmk.payload;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Kent on 10/14/2016.
 */

public abstract class AbstractPayload<T> implements Payload<T> {

	
	private static Logger logger = LoggerFactory.getLogger(AbstractPayload.class);
	
	/**
	 * The Payload Id
	 */
    private String id;
    
    /**
     * The string representation of the payload type
     */
    private int type;
    
    /**
     * The source session id
     */
    private String sourceId;
    
    /**
     * The list of intended destination session ids
     */
    private Set<String> destinationIds;
    
    /**
     * The data
     */
    private T data;

    private int status;
    
    public AbstractPayload() {
    	this.id = UUID.randomUUID().toString();
    	this.destinationIds = new HashSet<>(2);  // most of the time, we will have 2 destinations
    }
    
    /**
     * Request to add the specified destination id.
     * @param sessionId - the destination id
     */
    public void addDestination(String sessionId) {
    	
    	if (sessionId == null) {
    		throw new IllegalArgumentException("SessionId cannot be null");
    	}
    	
    	if (destinationIds.add(sessionId)){
			logger.debug("destination added: " + sessionId);
		}
    }
    
    /**
	 * @param destinationIds the list of destination session ids
	 */
	public void addDestinations(Set<String> destinationIds) {
		
		if (destinationIds == null || destinationIds.isEmpty()) {
			logger.warn("destinations are empty or null");
			return;
		}
		
		for (String id : destinationIds) {
    		addDestination(id);
    	}
	}

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

	/**
	 * @return the sourceId
	 */
	public String getSourceId() {
		return sourceId;
	}

	/**
	 * @param sourceId the source session Id to set
	 */
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	/**
	 * @return the list of destination session ids
	 */
	public Set<String> getDestinationIds() {
		return destinationIds;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}
}
