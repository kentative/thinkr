package com.bytes.fmk.service.leaderboard.ledger;

import java.time.OffsetDateTime;

/**
 * This represents a recordable action
 */
public interface Recordable {

	/**
	 * @return the categoryName
	 */
	String getCategoryName();

	
	/**
	 * @return the points
	 */
	int getPoints();
	
	
	/**
	 * 
	 * @return
	 */
	OffsetDateTime getTime();
	
	/**
	 * 
	 */
	void setTime(OffsetDateTime time);
	
}
