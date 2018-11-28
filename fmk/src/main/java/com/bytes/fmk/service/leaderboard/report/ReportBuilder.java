package com.bytes.fmk.service.leaderboard.report;

import java.util.List;

import com.bytes.fmk.service.leaderboard.Leaderboard;
import com.bytes.fmk.service.leaderboard.ScoreboardType;
import com.bytes.fmk.service.leaderboard.report.impl.RankReportBuilder;
import com.bytes.fmk.service.leaderboard.report.impl.StatsReportBuilder;

public interface ReportBuilder {

	public enum OrderType {
		/**
		 * Generates a report starting from the user's rank
		 */
		Rank,
		
		/**
		 * Generates a report started from the top ranks
		 */
		Top,
		
		/**
		 * Generates a report starting from the bottom ranks
		 */
		Bottom,
		
		All
	}
	
	public static final ReportBuilder DEFAULT = new RankReportBuilder().setScoreboardType(ScoreboardType.Team);
	
	public static final ReportBuilder USER_STATS = new StatsReportBuilder().setScoreboardType(ScoreboardType.User).includeTeamSummary(true);
	public static final ReportBuilder TEAM_STATS = new StatsReportBuilder().setScoreboardType(ScoreboardType.Team);
	public static final ReportBuilder GUILD_STATS = new StatsReportBuilder().setScoreboardType(ScoreboardType.Guild);
	
	public static final ReportBuilder USER = new RankReportBuilder().setScoreboardType(ScoreboardType.User).includeTeamSummary(true);
	public static final ReportBuilder TEAM = new RankReportBuilder().setScoreboardType(ScoreboardType.Team);
	public static final ReportBuilder GUILD = new RankReportBuilder().setScoreboardType(ScoreboardType.Guild);
	
	
	/**
	 * Request to build the report based on this builder configuration.
	 * @param leaderboard - the leaderboard with calculated data
	 * @return the report
	 */
	public Report build(Leaderboard leaderboard);
	
	public ReportBuilder setOrderType(OrderType reportType);

	public ReportBuilder setSummaryCategories(List<String> summaryCategories);

	public ReportBuilder setDetailCategories(List<String> detailCategories);

	public ReportBuilder setDetailExclusion(List<String> detailExclusion);

	public ReportBuilder setMaxEntries(int maxEntries);
	
	public ReportBuilder setScoreboardType(ScoreboardType type);
	
	public ReportBuilder includeTeamSummary(boolean includeTeamSummary);
}
