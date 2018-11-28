package com.bytes.fmk.service.leaderboard.report.impl;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bytes.fmk.service.Thinkr;
import com.bytes.fmk.service.leaderboard.Cycle;
import com.bytes.fmk.service.leaderboard.Leaderboard;
import com.bytes.fmk.service.leaderboard.LeaderboardService;
import com.bytes.fmk.service.leaderboard.Score;
import com.bytes.fmk.service.leaderboard.ScoreboardType;
import com.bytes.fmk.service.leaderboard.impl.Scoreboard;
import com.bytes.fmk.service.leaderboard.report.Report;
import com.bytes.fmk.service.leaderboard.report.ReportBuilder;
import com.bytes.fmk.service.leaderboard.report.ReportEntry;

/**
 * @author Kent
 *
 */
public abstract class ReportBuilderImpl implements ReportBuilder {

	/** Logger for this class */
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	/**
	 * The list of categories name to include in the summary
	 * If empty, will display default
	 */
	protected List<String>  summaryCategories;
	
	/**
	 * The ordered list of category names
	 * If empty, will display all
	 */
	protected List<String>  detailCategories;
	
	/**
	 * The list of categories to exclude from the details
	 */
	protected List<String>  detailExclusion;

	/**
	 * The maximum entries
	 */
	protected int maxEntries = 10;
	
	/**
	 * The report type
	 */
	protected OrderType orderType;
	
	/**
	 * The date time format
	 */
	protected String dateTimeFormat;
	
	/**
	 * The scoreboard type to report on.
	 * If not set, will default to the {@link ScoreboardType#User}
	 */
	protected ScoreboardType scoreboardType;

	
	/**
	 * Indicate if the team summary should be included in the footer section.
	 */
	protected boolean includeTeamSummary;
	
	
	public ReportBuilderImpl() {
		this.summaryCategories = new ArrayList<>();;
		this.detailCategories = new ArrayList<>();;
		this.detailExclusion = new ArrayList<>();
		this.orderType = OrderType.Top;
		this.dateTimeFormat = "MMM-dd";
		this.scoreboardType = ScoreboardType.User;
		this.includeTeamSummary = false;

		detailExclusion.add(Leaderboard.TOTAL);
	}
	
	
	/**
	 * Request to build the report based on this builder configuration.
	 * @param leaderboard - the leaderboard with calculated data
	 * @return the report
	 */
	public Report build(Leaderboard leaderboard) {
		
		if (leaderboard == null) {
			throw new IllegalArgumentException("Leadboard not found");
		}
		
		String leaderboardId = leaderboard.getId();
		logger.debug("Generating report: " + leaderboardId);
		
		LeaderboardService service = Thinkr.INSTANCE.getLeaderboardService();
		service.calculate(leaderboardId);
		
		ReportImpl report = new ReportImpl();
		buildReportSummary(report, leaderboard);
		buildReportHeader(report, leaderboard, scoreboardType);
		
		List<ReportEntry> reportEntries = buildReportEntries(report, leaderboard, scoreboardType);
		report.addEntries(reportEntries);
		
		if (includeTeamSummary) {
			buildTeamSummary(report, leaderboard);
		}
		
		buildReportFooter(report, leaderboard);
		return report;
	}

	
	protected void buildReportHeader(ReportImpl report, Leaderboard leaderboard, ScoreboardType type) {

		Scoreboard scoreboard = leaderboard.getScoreboards().get(type);
		report.setHeader(String.format(getHeaderFormat(),
				scoreboard.getEntries().size(),
				type.name(), 
				leaderboard.getCategoryNames().size() - 1));
	}

	protected abstract String getHeaderFormat();


	protected void buildReportFooter(ReportImpl report, Leaderboard leaderboard) {

		Duration remainingTime = Duration.between(OffsetDateTime.now(), leaderboard.getEndDate());
		String timeRemainingPattern;
		if (remainingTime.toMinutes() < 0) {
			timeRemainingPattern = "Inactive";
		} else {
			timeRemainingPattern = (leaderboard.getCycle() == Cycle.Daily) ? "%2$dh %3$dm remaining"
					: "%1$dd %2$dh %3$dm remaining";
		}

		String footer = String.format(timeRemainingPattern + System.lineSeparator() + getFooterFormat(),
				remainingTime.toDays(), 
				remainingTime.toHours() % 24, 
				remainingTime.toMinutes() % 60,
				leaderboard.getEndDate().format(DateTimeFormatter.ofPattern("MMM-dd HH:mm")), 
				leaderboard.getCycle());

		report.setFooter(footer);
	}


	protected abstract String getFooterFormat();


	/**
	 * Construct the report summary
	 * (i.e. Weekly User LeaderboardTitle)
	 * 
	 * @param report - the report to be appended to
	 * @param leaderboard - the leaderboard
	 */
	protected void buildReportSummary(ReportImpl report, Leaderboard leaderboard) {
		
		report.setSummary(
				String.format("%1$s %2$s %3$s", 
						leaderboard.getCycle(), 
						getScoreboardType(), 
						leaderboard.getTitle()));
	}
	
	
	/**
	 * 
	 * @param report - the report to be appended to
	 * @param leaderboard - the leaderboard
	 */
	protected void buildTeamSummary(ReportImpl report, Leaderboard leaderboard) {
		List<Score> teamRankedScores = Thinkr.INSTANCE.getLeaderboardService().listDescending(leaderboard.getId(),
				ScoreboardType.Team, Leaderboard.TOTAL, 1, leaderboard.getSize());

		StringBuilder teamTotal = new StringBuilder();
		teamRankedScores.forEach(s -> teamTotal.append(s.getEntryId() + " - " + s.getPoints() + "|"));

		String teamSummary = (teamTotal.length() > 0) ? teamTotal.substring(0, teamTotal.length() - 1) : "";

		report.setTeamSummary(teamSummary);
	}

	
	/**
	 * Request to build the entries for the report
	 * @param report - the report
	 * @param leaderboard - the leaderboard
	 * @param scoreboardType - the scoreboard type
	 */
	private List<ReportEntry> buildReportEntries(ReportImpl report, Leaderboard leaderboard, ScoreboardType scoreboardType) {

		Thinkr.INSTANCE.getLeaderboardService().calculate(leaderboard.getId());
		List<Score> rankedScores = getRankedScores(leaderboard);
		
		Scoreboard scoreboard = leaderboard.getScoreboards().get(scoreboardType);
		List<ReportEntry> reports = new ArrayList<>();
		int max = (rankedScores.size() < maxEntries || maxEntries < 0) ?rankedScores.size() :maxEntries;
		for (int i = 0; i < max; i++) {
			ReportEntryImpl entry = new ReportEntryImpl();
			
			// Summary: Rank# Name - Total
			Score score = rankedScores.get(i);
			entry.buildSummary(i+1, score.getEntryId(), score);
			
			// Details: Category1: points | Category2: points | ... 
			Map<String, Score> userScores = scoreboard.getScores(score.getEntryId());
			for (String categoryName : userScores.keySet()) {
				
				if (getDetailExclusion().contains(categoryName)) continue;
				entry.appendDetails(userScores.get(categoryName));
			}
			
			reports.add(entry);
		}
		
		return reports;
	}
	
	protected List<Score> getRankedScores(Leaderboard leaderboard) {

		switch (orderType) {
		case Bottom:
			return Thinkr.INSTANCE.getLeaderboardService().listAscending(
					leaderboard.getId(), 
					scoreboardType, 
					Leaderboard.TOTAL, 
					leaderboard.getSize(), maxEntries);
			
		case Top:
			return Thinkr.INSTANCE.getLeaderboardService().listDescending(
					leaderboard.getId(), 
					scoreboardType, 
					Leaderboard.TOTAL, 
					1, maxEntries);
		default:
			return Thinkr.INSTANCE.getLeaderboardService().listDescending(
					leaderboard.getId(), 
					scoreboardType, 
					Leaderboard.TOTAL, 
					1, leaderboard.getSize());
		
		}
	}


	public OrderType getOrderType() {
		return orderType;
	}

	public ReportBuilderImpl setOrderType(OrderType orderType) {
		this.orderType = orderType;
		return this;
	}


	public List<String> getSummaryCategories() {
		return summaryCategories;
	}


	public ReportBuilderImpl setSummaryCategories(List<String> summaryCategories) {
		this.summaryCategories = summaryCategories;
		return this;
	}


	public List<String> getDetailCategories() {
		return detailCategories;
	}


	public ReportBuilderImpl setDetailCategories(List<String> detailCategories) {
		this.detailCategories = detailCategories;
		return this;
	}


	public List<String> getDetailExclusion() {
		return detailExclusion;
	}


	public ReportBuilderImpl setDetailExclusion(List<String> detailExclusion) {
		this.detailExclusion = detailExclusion;
		return this;
	}


	public int getMaxEntries() {
		return maxEntries;
	}


	public ReportBuilderImpl setMaxEntries(int maxEntries) {
		this.maxEntries = maxEntries;
		return this;
	}


	public String getDateTimeFormat() {
		return dateTimeFormat;
	}


	public ReportBuilderImpl setDateTimeFormat(String dateTimeFormat) {
		this.dateTimeFormat = dateTimeFormat;
		return this;
	}


	public ScoreboardType getScoreboardType() {
		return scoreboardType;
	}


	public ReportBuilder setScoreboardType(ScoreboardType scoreboardType) {
		this.scoreboardType = scoreboardType;
		return this;
	}

	
	public ReportBuilder includeTeamSummary(boolean includeTeamSummary) {
		this.includeTeamSummary = includeTeamSummary;
		return this;
	}
	
}
