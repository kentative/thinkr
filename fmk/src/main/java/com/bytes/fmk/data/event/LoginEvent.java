package com.bytes.fmk.data.event;

public class LoginEvent {
	
	public enum Type {
	
		/** A response to indicate user registration successful */
		RegisterOK,
		
		/** A response to indicate user registration failed */
		RegisterFailed,
		
		/** A response to indicate successful user login*/
		LoginOK,
		
		/** A response to indicate failed user login*/
		LoginFailed
	}
	
	private Type type;
	
	private String information;

	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * @return the information
	 */
	public String getInformation() {
		return information;
	}

	/**
	 * @param information
	 *            the information to set
	 */
	public void setInformation(String information) {
		this.information = information;
	}

}
