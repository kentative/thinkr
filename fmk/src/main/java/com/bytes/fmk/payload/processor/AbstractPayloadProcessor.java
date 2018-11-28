package com.bytes.fmk.payload.processor;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bytes.fmk.payload.Payload;
import com.google.gson.Gson;

public abstract class AbstractPayloadProcessor implements PayloadProcessor<String> {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	protected Gson gson;

	protected Set<ProcessorListener> listeners;

	/**
	 * Constructor
	 */
	public AbstractPayloadProcessor() {
		this.gson = new Gson();
		this.listeners = new HashSet<ProcessorListener>();
	}

	/**
	 * Must be a {@code FighterPayload} and have a sourceId
	 */
	@Override
	public boolean validate(Payload<String> p) {

		logger.info(String.format("VALIDATING payload (type.data) - %1$s.%2$s", p.getType(), p.getDataType()));

		if (p.getSourceId() == null) {
			logger.error("Payload sourceId is not specified");
			return false;
		}
		return true;
	}

	@Override
	public void addListener(ProcessorListener listener) {
		if (listener != null) {
			listeners.add(listener);
		}
	}

	@Override
	public boolean removeListener(ProcessorListener listener) {

		if (listener != null) {
			return listeners.remove(listener);
		}
		return false;
	}

	public <EventType> void notify(ProcessorEvent<EventType> event) {
		for (ProcessorListener listener : listeners) {
			listener.onEvent(event);
		}
	}
	
	public <EventType> void notify(String data) {
		for (ProcessorListener listener : listeners) {
			listener.onEvent(data);
		}
	}
}
