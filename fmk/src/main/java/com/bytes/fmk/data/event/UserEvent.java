package com.bytes.fmk.data.event;

import com.bytes.fmk.data.model.User.Status;

public class UserEvent {

	public enum Type {
		
		/** Default type to indicate probable code error */
		NotDefined,

		/** An event to indicate user state has changed, (i.e., Offline, Online) */
		StateUpdate,
		
		/** An event to indicate a new user has been registered */
		UserAdded
	}
	
	private Type type;
	private String userId;
	private Status status;

	public UserEvent(Type type, String id, Status status) {
		this.type = type;
		this.userId = id;
		this.status = status;
	}

	/**
	 * @return the status
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}
}
