package com.bytes.fmk.payload.processor;

import com.bytes.fmk.payload.Payload;

/**
 * Created by Kent on 10/14/2016.
 */

public interface PayloadProcessor<T> {

	/**
	 * Performs payload validation
	 * @param payload
	 * @return
	 */
	boolean validate(Payload<T> payload);
	
	/**
	 * 
	 * @param payload
	 * @return
	 */
	Payload<T> process(Payload<T> payload);
	
	void addListener(ProcessorListener listener);
	
	boolean removeListener(ProcessorListener listener);
	
	<EventType> void notify(ProcessorEvent<EventType> event);

}
