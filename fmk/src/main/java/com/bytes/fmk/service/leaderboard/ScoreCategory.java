package com.bytes.fmk.service.leaderboard;

import java.time.OffsetDateTime;
import java.util.Comparator;

public class ScoreCategory implements Comparator<Score> {
	
	private String name;
	
	private boolean isStaled;
	
	/**
	 * The last time this leader ran the rank calculation
	 */
	private OffsetDateTime calculatedTime;
	
	public ScoreCategory() {
		this(null);
	}

	/**
	 * Create an new category. 
	 * @param name - the name of the category
	 */
	public ScoreCategory(String name) {
		this.name = name;
		this.isStaled = true;
		this.calculatedTime = null;
	}

	@Override
	public int compare(Score s1, Score s2) {
		
		long p1 = s1.getPoints();
		long p2 = s2.getPoints();
		if (p1 > p2) return -1;
		if (p1 < p2) return 1;
		
		return s1.getEntryId().compareTo(s2.getEntryId());
	}
	
	public boolean equals(Object obj) {
		
		if(obj != null && obj instanceof ScoreCategory) {
			return name.equals(((ScoreCategory) obj).getName());
		}
		return false;
	}
	
	public int hashCode() {
		return name.hashCode();
	}

	public String getName() {
		return name;
	}

	public boolean isStaled() {
		return isStaled;
	}

	public void setStaled(boolean isStaled) {
		this.isStaled = isStaled;
	}

	public OffsetDateTime getCalculatedTime() {
		return calculatedTime;
	}

	public void setCalculatedTime(OffsetDateTime calculatedTime) {
		this.calculatedTime = calculatedTime;
	}
}
