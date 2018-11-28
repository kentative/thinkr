package com.bytes.fmk.data.model;

import java.util.ArrayList;
import java.util.List;

import com.bytes.fmk.data.event.UserEvent;
import com.bytes.fmk.observer.IObservable;
import com.bytes.fmk.observer.IObserver;

public class User implements IObservable<UserEvent> {

	private transient IObserver<UserEvent> observer;
	
	/**
	 * Player Status
	 */
	public enum Status {
		
		/**
		 * Not yet registered user
		 */
		New, 

		/**
		 * Available for match making
		 */
		Available,
		

		Offline,
		
		/**
		 * Searching for game
		 */
		Searching,

		/**
		 * Ready for battle to start
		 */
		Ready,

		/**
		 * In active battle
		 */
		Battle;
	}

	/**
	 * This can be the email address
	 */
	private String id;
	
	/**
	 * This is the display name, doesn't have to be unique
	 */
	private String displayName;
	
	private Status status;
	private String avatarId;
	
	
	private List<Team> teams;

	/**
	 * Default constructor
	 * @param id the display name
	 */
	public User(String id) {
		this(id, id);
	}
	
	
	/**
	 * Default constructor
	 * @param id the user id
	 * @param displayName - the user display name
	 */
	public User(String id, String displayName) {
		this(id, displayName, new Team(), new Guild());
	}


	/**
	 * Default constructor
	 * @param id the user id
	 * @param displayName - the user display name
	 * @param team - the default team
	 * @param guild - the default guild
	 */
	public User(String id, String displayName, Team team, Guild guild) {
		this(id, displayName, team, guild, Status.New);
		
	}
	
	
	/**
	 * Constructs a user with the specified id, name and status
	 * @param id the unique identifier such as email address
	 * @param displayName the display name
	 * @param team - the default team
	 * @param guild - the default guild
	 * @param status the default status, usually {@link Status#Available}
	 */
	public User(String id, String displayName, Team team, Guild guild, Status status) {
		this.id = id;
		this.displayName = displayName;
		this.status = status;
		this.teams = new ArrayList<>();
		
		addTeam(team);
		team.addGuild(guild);
	}
	
	
	@Override
	public boolean equals(Object obj) {
		
		if(obj instanceof User) {
			String id1 = getId();
			String id2 = ((User) obj).getId();
			
			if (id1 == null || id2 == null) {
				return id1 == id2;
			}
			
			return id1.equals(id2);
		}
		return false;
	}

	@Override
	public int hashCode() {
		if (getId() == null) return 0;
		return getId().hashCode();
	}
	
	/**
	 * @return the displayName
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * @param displayName
	 *            the displayName to set
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * @return the status
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * @param status - the status to set
	 * @param sourceSessionId - the source session id
	 */
	public User setStatus(Status status) {
		this.status = status;
		return this;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public User setId(String id) {
		this.id = id;
		return this;
	}

	/**
	 * @return the avatarId
	 */
	public String getAvatarId() {
		return avatarId;
	}

	/**
	 * @param avatarId the avatarId to set
	 */
	public void setAvatarId(String avatarId) {
		this.avatarId = avatarId;
	}

	/**
	 * @return the observer
	 */
	public IObserver<UserEvent> getObserver() {
		return observer;
	}

	/**
	 * @param observer the observer to set
	 */
	public void setObserver(IObserver<UserEvent> observer) {
		this.observer = observer;
	}
	
	/**
	 * Assign a team to the user
	 * @param team
	 * @return true if successfully added.
	 */
	public boolean addTeam(Team team) {
		return teams.add(team);
	}


	public List<Team> getTeams() {
		return teams;
	}
	
	public Team getTeam() {
		if (teams.isEmpty()) {
			return new Team();
		}
		return teams.get(0);
	}
}
