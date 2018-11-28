package com.bytes.fmk.service.leaderboard.report.impl;

import com.bytes.fmk.data.model.User;
import com.bytes.fmk.service.Thinkr;
import com.bytes.fmk.service.leaderboard.Score;
import com.bytes.fmk.service.leaderboard.report.ReportEntry;
import com.bytes.fmk.service.user.UserService;

/**
 * Report format
 * 
 * <pre>
 *  
 * Rank1. UserName - Total: Points
 * Category1: points | Category2: points | Category3: points ...
 * 
 * Rank2. UserName - Total: Points
 * Category1: points | Category2: points | Category3: points ...
 * 
 * Rank3. UserName - Total: Points
 * Category1: points | Category2: points | Category3: points ...
 * 
 * </pre>
 */
public class ReportEntryImpl implements ReportEntry {
	
	
	/**
	 * The first line consisting of
	 * Rank1 - UserName - Total : Points
	 */
	private String summary;
	
	/**
	 * The detailed line, consisting of
	 * Category1: points | Category2: points | Category3: points ...
	 */
	private StringBuilder details;
	
	/**
	 * 
	 * @param rank
	 * @param userId
	 * @param total
	 * @return
	 */
	public ReportEntryImpl buildSummary(int rank, String userId, Score score) {
		
		UserService<User> userService = Thinkr.INSTANCE.getUserService();
		String userName = (userService.isRegistered(userId)) 
				?userService.getUser(userId).getDisplayName() :userId;
		
		summary = String.format(
				"%1$d. %2$s - Total: %3$d", 
				rank, userName, score.getPoints());
		return this;
	}
	
	/**
	 * 
	 * @param scores
	 * @return
	 */
	public ReportEntryImpl appendDetails(Score score) {
		
		StringBuilder sb = new StringBuilder();
		sb.append(String.format(
				"%1$s: %2$d", 
				score.getCategoryName(),
				score.getPoints()));
		
		if (sb.length() > 3) {
			if (details == null) {
				details = new StringBuilder();
				details.append(sb.toString());
			} else {
				details.append("|" + sb.toString());
			}
		}
		return this;
	}
	
	public String toString() {
		if (getDetails() != null) {
			return getSummary() + System.lineSeparator() + getDetails();
		}
		return getSummary();
		
	}

	public String getSummary() {
		return "**" + summary + "**";
	}

	public String getDetails() {
		if (details != null) {
			return details.toString();
		} 
		return null;
			
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public void setDetails(String details) {
		this.details = new StringBuilder(details);
	}
}
