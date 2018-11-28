package com.bytes.fmk.service.leaderboard;

public class Score { 

	/**
	 * This allows the sorted score list to correlates to an entry.
	 */
	private String entryId;
	
	/**
	 * This allows the score to be sorted.
	 */
	private long points;
	
	/**
	 * This allows the selection of score by category
	 *  -- might not be needed
	 */
	private String categoryName;

	
	/**
	 * Default constructor
	 * @param entryId 
	 */
	public Score(String entryId, String categoryName) {
		this.entryId = entryId;
		this.categoryName = categoryName;
		this.points = 0;
	}
	
	/**
	 * @return the points
	 */
	public long getPoints() {
		return points;
	}

	/**
	 * @param points
	 *            the points to set
	 */
	public void setPoints(long points) {
		this.points = points;
	}
	
	@Override
	public String toString() {
		return String.format("%1$d %2$s - %3$s", points, categoryName, entryId); 
	}

	@Override
	public boolean equals(Object score) {
		
		if (! (score instanceof Score)) {
			return false;
		}
		
		Score s2 = (Score) score;
		if (entryId == null && s2.getEntryId() == null) {
			return true;
		}
		
		if (entryId != null && s2.getEntryId() != null) {
			return entryId.equalsIgnoreCase(s2.getEntryId());
		}
		return false;		
	}
	
	@Override
	public int hashCode() {
		int hash = entryId.hashCode();
		return hash * 17 + categoryName.hashCode();
	}

	/**
	 * @return the userId
	 */
	public String getEntryId() {
		return entryId;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(String userId) {
		this.entryId = userId;
	}

	/**
	 * 
	 * @param points the points to be added
	 */
	public void add(long points) {
		this.points += points;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
}
