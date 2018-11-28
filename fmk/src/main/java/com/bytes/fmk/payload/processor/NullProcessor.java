package com.bytes.fmk.payload.processor;

import com.bytes.fmk.payload.Payload;

public class NullProcessor extends AbstractPayloadProcessor {
	
	@Override
	public boolean validate(Payload<String> payload) {
		return true;
	}

	@Override
	public void addListener(ProcessorListener listener) {

	}

	@Override
	public boolean removeListener(ProcessorListener listener) {
		return true;
	}

	@Override
	public <EventType> void notify(ProcessorEvent<EventType> event) {
		
	}

	@Override
	public Payload<String> process(Payload<String> payload) {
		return null;
	}
}
