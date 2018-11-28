package com.bytes.fmk.observer;

public enum DestinationType {
	
	/** All connected users */
	All, 
	
	/** All connected users except the source */
	Other, 
	
	/** The destination is the same as the source */
	Source, 
	
	/** The list of destination is specified */
	Custom
	
}