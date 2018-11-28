package com.bytes.fmk.service.leaderboard.report;

import java.util.concurrent.ConcurrentHashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.bytes.fmk.data.model.Guild;
import com.bytes.fmk.data.model.Team;
import com.bytes.fmk.data.model.User;
import com.bytes.fmk.service.Thinkr;
import com.bytes.fmk.service.leaderboard.Cycle;
import com.bytes.fmk.service.leaderboard.Leaderboard;
import com.bytes.fmk.service.leaderboard.LeaderboardService;
import com.bytes.fmk.service.leaderboard.ledger.impl.RecordableScore;
import com.bytes.fmk.service.leaderboard.report.Report;
import com.bytes.fmk.service.leaderboard.report.ReportBuilder;

import mockit.Deencapsulation;

public class ReportBuilderImplTest {
	
	private static final int MAX_USERS = 50;
	private LeaderboardService leaderboardService;
	private String leaderboardId;
	private Leaderboard leaderboard;
	private String leaderboardTitle = "Thinkr Leaderboard";
	
	
	private String category1 = "Raid";
	private String category2 = "Wild";
	private String category3 = "Shinies";
	
	/**
	 * Register 50 users and generate scores
	 */
	@Before
	public void setup() {
		
		// Reset service
		Deencapsulation.setField(
				Thinkr.INSTANCE, 
				"services", new ConcurrentHashMap<>());
		
		leaderboardService = Thinkr.INSTANCE.getLeaderboardService();
		leaderboard = leaderboardService.create(leaderboardTitle, Cycle.Daily);
		leaderboardId = leaderboard.getId();

		category1 = "Raid";
		category2 = "Wild";
		category3 = "Shinies";
		
		leaderboard.addCategories(category1);
		leaderboard.addCategories(category2);
		leaderboard.addCategories(category3);
		
		Team t1 = new Team("team01");
		Team t2 = new Team("team02");
		
		Guild guild = new Guild("Ascalon");
		
		for (int i = 0; i < MAX_USERS; i++) {
			String userId = "UserId" + (i + 1);
			Team team = (i%2 ==0) ?t1 :t2;
			leaderboardService.addUser(leaderboardId, new User(userId, userId + "Name", team, guild));
			
			leaderboardService.update(leaderboardId, userId, RecordableScore.Point);
			leaderboardService.update(leaderboardId, userId, RecordableScore.Participation);
			
		}
	}
	
	@Test
	public void generateReport() {
		
		Report defaultReport = leaderboardService.generateReport(leaderboardId, ReportBuilder.DEFAULT);
		Assert.assertEquals(10, defaultReport.getEntries().size());
		System.out.println(defaultReport);
		
	}
	
	@Test
	public void generateReportForUser() {
		
		Report userReport = leaderboardService.generateReport(leaderboardId, ReportBuilder.USER.setMaxEntries(20));
		Assert.assertEquals(20, userReport.getEntries().size());
		
		System.out.println(userReport);
		
	}
	
	@Test
	public void generateReportForTeam() {
		
		Report teamReport = leaderboardService.generateReport(leaderboardId, ReportBuilder.TEAM);
		Assert.assertEquals(2, teamReport.getEntries().size());
		System.out.println(teamReport);
		
	}
	
	@Test
	public void generateReportForGuild() {
		
		Report guildReport = leaderboardService.generateReport(
				leaderboardId, 
				ReportBuilder.GUILD);
		Assert.assertEquals(1, guildReport.getEntries().size());
		System.out.println(guildReport);
		
	}
	
	@Test
	public void generateReportForStat() {
		
		Report report = leaderboardService.generateReport(
				leaderboardId, 
				ReportBuilder.TEAM_STATS);
		Assert.assertEquals(2, report.getEntries().size());
		System.out.println(report);
		
	}
}
