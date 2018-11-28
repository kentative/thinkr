package com.bytes.fmk.data.model;

import java.util.ArrayList;
import java.util.List;

public class Team {
	
	public static final String UNDEFINED = "NO_TEAM";
	
	private String name;
	
	private List<Guild> guilds;
	
	public Team() {
		this(UNDEFINED);
	}
	
	public Team(String name) {
		this.name = name;
		this.guilds = new ArrayList<>();
	}
	
	
	/**
	 * Assign a guild to the team
	 * @param guild
	 * @return true if successfully added.
	 */
	public boolean addGuild(Guild guild) {
		return guilds.add(guild);
	}
	

	public String getName() {
		return name;
	}

	
	public void setName(String name) {
		this.name = name;
	}

	public Guild getGuild() {
		if (guilds.isEmpty()) {
			return new Guild();
		}
		return guilds.get(0);
	}

}
