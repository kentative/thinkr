package com.bytes.fmk.observer;

public class Context {

	private DestinationType destinationType;
	private String source;
	
	public Context() {
		this(DestinationType.All);
	}
	
	public Context(DestinationType destinationType) {
		this.destinationType = destinationType;
	}
	
	/**
	 * @return the destinationType
	 */
	public DestinationType getDestinationType() {
		return destinationType;
	}
	/**
	 * @param destinationType the destinationType to set
	 */
	public void setDestinationType(DestinationType destinationType) {
		this.destinationType = destinationType;
	}
	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}
	/**
	 * @param source the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}
	
}
