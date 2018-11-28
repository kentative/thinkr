package com.bytes.fmk.service.leaderboard.report;

import java.util.List;

/**
 * Report format
 * 
 * 
 * <pre>
 * [optional] Summary
 * 
 * [optional] Header
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
 * [optional] Footer
 * </pre>
 */
public interface Report {
	
	
	/**
	 * The report title and description
	 * @return the report summary
	 */
	String getSummary();

	
	/**
	 * The report header contains data description, column information, etc...
	 * @return the report header
	 */
	String getHeader();
	
	
	/**
	 * The report footer contains the total number of users,
	 * score max, min, average, active dates, etc..
	 * @return the report footer
	 */
	String getFooter();
	
	
	/**
	 * The report entries contains all of the requested information.
	 * @return the report data
	 */
	List<ReportEntry> getEntries();
	
	
	
	String getTeamSummary();
	
}
