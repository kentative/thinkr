package com.bytes.fmk.payload.processor;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Kent on 10/14/2016.
 * A default abstract implementation of the PayloadProcessor registry for 
 * Payload of type String.
 */
public abstract class PayloadProcessorRegistry {

	/** Logger for this class */
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	/** Null processor for invalid request */
    protected final AbstractPayloadProcessor nullHandler;

    /** The map of registered processors */
    protected final Map<String, AbstractPayloadProcessor> processors;

    public PayloadProcessorRegistry() {

        processors = new ConcurrentHashMap<>(10);
        nullHandler = new NullProcessor();
	}
    

    /**
     * Retrieve the {@code PayloadProcessor} for the specified type.
     * If a handler is not defined, no actions is performed
     * @param payloadType
     * @return
     */
    public PayloadProcessor<String> getProcessor(String payloadType) {
        String key = payloadType.toUpperCase();
        if (!processors.containsKey(key)) {
        	logger.warn("Redirecting request to null processor: " + payloadType);
            return nullHandler;

        }
        return (processors.get(key));
    }

    /**
     * Registers a {@code PayloadProcessor} with the specified payload key.
     *
     * @param key the payload key
     * @param handler the {@code PayloadProcessor} to be used for the specified key
     * @return true if the payload handler was successfully registered
     */
    public boolean registerProcessor(String key, AbstractPayloadProcessor handler) {

        if (!key.isEmpty()) {
            return (processors.put(key, handler) != null);
        }
        return false;
    }
    
    /**
     * Unregisters a {@code PayloadProcessor} with the specified payload key.
     *
     * @param key the payload key
     * @param handler the {@code PayloadProcessor} to be used for the specified key
     * @return the removed processor
     */
    public AbstractPayloadProcessor unregisterProcessor(String key) {

        if (!key.isEmpty() && processors.containsKey(key)) {
            return processors.remove(key);
        }
        return null;
    }
}
