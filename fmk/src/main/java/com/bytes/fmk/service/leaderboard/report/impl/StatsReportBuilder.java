package com.bytes.fmk.service.leaderboard.report.impl;

/**
 * @author Kent
 *
 */
public class StatsReportBuilder extends ReportBuilderImpl {

	@Override
	protected String getHeaderFormat() {
		return "%1$d %2$ss, participating in %3$d categories";
	}

	@Override
	protected String getFooterFormat() {
		return "%5$s scoring ends on %4$s";
	}
}
