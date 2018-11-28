package com.bytes.fmk.service.leaderboard;

import java.time.OffsetDateTime;

import com.bytes.fmk.service.leaderboard.ledger.Recordable;


/**
 * 
 */
public class Scorable implements Recordable {
	
	private String categoryName;
	private int points;
	private OffsetDateTime time;
	
	public Scorable(String categoryName, int points) {
		this(categoryName, points, null);
	}
	
	public Scorable(String categoryName, int points, OffsetDateTime time) {
		this.categoryName = categoryName;
		this.points = points;
		this.time = time;
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
