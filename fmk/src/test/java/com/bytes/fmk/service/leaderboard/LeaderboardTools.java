package com.bytes.fmk.service.leaderboard;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import org.junit.Assert;

import com.bytes.fmk.service.Thinkr;
import com.bytes.fmk.service.leaderboard.impl.LeaderboardServiceImpl;
import com.bytes.fmk.service.leaderboard.report.ReportBuilder;
import com.bytes.fmk.service.persistence.PersistenceMode;
import com.google.gson.reflect.TypeToken;

import mockit.Deencapsulation;

/**
 * Various tooling functions to work with leaderboard.
 * These are not tests but they have test library dependencies such as JMockit 
 * {@code Deencapsulation}. This allows more flexibility without exposing the API 
 * surface.
 * 
 * @author Kent
 */
public class LeaderboardTools {

//	private static final String LEADERBOARD_ID = "Bulba Leaderboard v1.0";
//	private static final String LEADERBOARD_ID = "d6f3ec3c-8932-4a7c-bac2-9812a9de4a84";
	private static final String LEADERBOARD_ID = "cb72e7c5-c643-4204-aff4-dd19eaecec7b";
	
	
	// Latest cb72e7c5-c643-4204-aff4-dd19eaecec7b_SATURDAY
	public static void main(String...args) {
		
		// Performs backup and print a report
		String leaderboardId = backupToLocal("BulbaLeaderboardServiceResourceID", "Bulba_Scoreboard");
		reportLocal(leaderboardId);
		
	}
	
	
	/**
	 * Export data from Azure Redis to local instance of Redis
	 * This requires a local instance of Redis active.
	 */
	private static String backupToLocal(String serviceId, String setId) {
		
		LeaderboardService service = Thinkr.INSTANCE.getLeaderboardService();
		
		// Load leaderboard service
		service.getSerializer().registerResourceID(
				serviceId, 
				new TypeToken<LeaderboardServiceImpl>(){}.getType());
		service.load(PersistenceMode.RedisAzure, serviceId);
		
		// Get LeaderboardSet, generate leaderboardId based on cycle and time
//		LeaderboardSet set = service.getLeaderboardSet(setId);
//		String leaderboardDailyId = set.generateLeaderboardId(Cycle.Daily, OffsetDateTime.now().withMonth(6).withDayOfMonth(30));
		
		// Load leaderboard
		Leaderboard leaderboard = service.getLeaderboard(setId, Cycle.Daily, OffsetDateTime.now().withMonth(6).withDayOfMonth(30));
//		Leaderboard leaderboard = service.load(leaderboardDailyId, PersistenceMode.RedisAzure);
		
		String localLeaderboardId = leaderboard.getId();
//		Deencapsulation.setField(leaderboard, "id", getLocalLeaderboardId());
		System.out.println("Backing up to local redis with id: " + localLeaderboardId);
		Assert.assertTrue(
				"Backing up azure to local" + localLeaderboardId,
				service.save(leaderboard, PersistenceMode.RedisLocal));
		return localLeaderboardId;
	}
	

	/**
	 * Generate a leaderboard report using data from the local Redis instance.
	 */
	private static void reportLocal(String leaderboardId) {
		LeaderboardService service = Thinkr.INSTANCE.getLeaderboardService();
		service.load(leaderboardId, PersistenceMode.RedisLocal);
		System.out.println(service.generateReport(leaderboardId, ReportBuilder.DEFAULT.setMaxEntries(-1)));
	}
	
	/**
	 * Generate the local leaderboard id based on the LEADERBOARD_ID. To prevent overwriting good data, 
	 * 7 versions are create, one per day of week. If there is corruption, use the previous day data.
	 * @return the leaderboard id (the redis resourceId) to load from the local Redis instance
	 */
	private static String getLocalLeaderboardId() {
		return LEADERBOARD_ID + "_" + LocalDate.now().getDayOfWeek();
	}
	
}
