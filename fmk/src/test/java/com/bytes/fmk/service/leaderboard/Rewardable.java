package com.bytes.fmk.service.leaderboard;

import java.time.OffsetDateTime;

import com.bytes.fmk.service.leaderboard.ledger.Recordable;


/**
 * 
 */
public enum Rewardable implements Recordable {
	
	ONE_KARMA("Karma",   1),
	TWO_KARMA("Karma",   2),
	THREE_KARMA("Karma", 3),
	TEN_KARMA("Karma",  10); 
	
	private String categoryName;
	private int points;
	private OffsetDateTime time;
	
	private Rewardable(String categoryName, int points) {
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

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public void setPoints(int points) {
		this.points = points;
	}
}
