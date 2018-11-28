package com.bytes.fmk.service.user;

import com.bytes.fmk.data.model.Guild;
import com.bytes.fmk.data.model.Team;
import com.bytes.fmk.data.model.User;

public class AlphaUser extends User {

	private String friendshipCode;

	/**
	 * 
	 * @param id
	 * @param displayName
	 * @param team
	 */
	public AlphaUser(String id, String displayName, Team team) {
		super(id, displayName, team, new Guild("Fullerton"), Status.New);
	}

	
	public AlphaUser(String id, String displayName) {
		super(id, displayName);
	}


	/**
	 * @return the friendshipCode
	 */
	public String getFriendshipCode() {
		return friendshipCode;
	}

	
	/**
	 * @param friendshipCode the friendshipCode to set
	 */
	public void setFriendshipCode(String friendshipCode) {
		this.friendshipCode = friendshipCode;
	}

}
