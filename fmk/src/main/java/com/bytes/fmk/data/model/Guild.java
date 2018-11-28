package com.bytes.fmk.data.model;

public class Guild {
	
	public static final String UNDEFINED = "NO_TEAM";
	
	private String name;

	public Guild() {
		this(UNDEFINED);
	}
	
	public Guild(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
