package com.bytes.fmk.service.leaderboard.ledger.impl;

import java.time.OffsetDateTime;

import com.bytes.fmk.service.leaderboard.ledger.Recordable;


/**
 * 
 */
public enum RecordableScore implements Recordable {
	
	Point("Points", 1),
	
	Participation("Karma", 1);
	
	private String categoryName;
	private int points;
	private OffsetDateTime time;
	
	private RecordableScore(String categoryName, int points) {
		this.categoryName = categoryName;
		this.points = points;
	}

	/**
	 * @return the categoryName
	 */
	public String getCategoryName() {
		return categoryName;
	}

	/**
	 * @return the points
	 */
	public int getPoints() {
		return points;
	}

	/**
	 * @return the time
	 */
	public OffsetDateTime getTime() {
		return time;
	}

	/**
	 * @param time the time to set
	 */
	public void setTime(OffsetDateTime time) {
		this.time = time;
	}
}
