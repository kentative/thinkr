package com.bytes.fmk.data.event;

/**
 * A generic event listener
 * @author Kent
 *
 */
public interface EventListener extends java.util.EventListener {

	void onEvent(String message);
	
}
