package com.bytes.fmk.service.leaderboard.report.impl;

import java.util.ArrayList;
import java.util.List;

import com.bytes.fmk.service.leaderboard.report.Report;
import com.bytes.fmk.service.leaderboard.report.ReportEntry;

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
public class ReportImpl implements Report {
	
	
	private String summary;
	
	private String header;
	
	private String footer;
	
	private String teamSummary;
	
	private List<ReportEntry> entries;

	public ReportImpl() {
		this.summary = "Summary";
		this.header = "Header";
		this.teamSummary = null;
		this.footer = "Footer";
		this.entries = new ArrayList<>();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getSummary()).append(System.lineSeparator());
		sb.append(getHeader()).append(System.lineSeparator());
		entries.forEach(x -> sb.append(x).append(System.lineSeparator()));
		sb.append(getFooter()).append(System.lineSeparator());
		return sb.toString();
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getFooter() {
		return footer;
	}

	public void setFooter(String footer) {
		this.footer = footer;
	}

	public List<ReportEntry> getEntries() {
		return entries;
	}

	public void addEntries(List<ReportEntry> entries) {
		this.entries.addAll(entries);
	}

	public String getTeamSummary() {
		return teamSummary;
	}

	public void setTeamSummary(String teamSummary) {
		this.teamSummary = teamSummary;
	}
	
}
