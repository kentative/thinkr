package com.bytes.fmk.service.leaderboard.ledger;

import java.time.OffsetDateTime;

public class LedgerEntry implements Recordable {

	
	private String categoryName;

	private OffsetDateTime time;
	
	private int points;
	

	public LedgerEntry() {
		this(null, -1, null);
	}
	
	public LedgerEntry(String name, int points, OffsetDateTime time) {
		this.categoryName = name;
		this.points = points;
		this.time = time;
	}
	
	public String toString() {
		return categoryName + " " + points + " " + time;
	}

	public OffsetDateTime getTime() {
		return time;
	}
	
	public void setTime(OffsetDateTime time) {
		this.time = time;
	}

	public int getPoints() {
		return points;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
}
