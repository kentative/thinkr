package com.bytes.fmk.service.leaderboard;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.bytes.fmk.service.Thinkr;
import com.bytes.fmk.service.Thinkr.Service;
import com.bytes.fmk.service.ThinkrService;
import com.bytes.fmk.service.leaderboard.impl.LeaderboardImpl;
import com.bytes.fmk.service.leaderboard.impl.LeaderboardServiceSerializer;
import com.bytes.fmk.service.leaderboard.impl.LeaderboardSetImpl;
import com.bytes.fmk.service.leaderboard.ledger.Ledger;
import com.bytes.fmk.service.leaderboard.ledger.LedgerEntry;
import com.bytes.fmk.service.leaderboard.report.Report;
import com.bytes.fmk.service.leaderboard.report.impl.ReportBuilderImpl;
import com.bytes.fmk.service.persistence.PersistenceMode;

import mockit.Deencapsulation;

public class LeaderboardServiceTest extends LeaderboardServiceTestBase {

	private String title = "Highscore";
	
	private String category = "Karma";

	
	@Test
	public void basicFlow() {

		Leaderboard leaderboard = service.create(title, Cycle.Monthly);
		String leaderboardId = leaderboard.getId();
		
		Assert.assertEquals(title, leaderboard.getTitle());

		// Register 3 users
		service.addUser(leaderboardId, user01);
		service.addUser(leaderboardId, user02);
		service.addUser(leaderboardId, user03);
		Assert.assertEquals(3, leaderboard.getSize());

		// Update user score
		leaderboard.setAutoUpdate(true);
		service.update(leaderboard.getId(), user01.getId(), Rewardable.ONE_KARMA);
		Assert.assertEquals("Checking first place " + user01.getId(), 1, service.getRank(leaderboardId, Leaderboard.TOTAL, user01.getId()));

		service.update(leaderboard.getId(), user02.getId(), Rewardable.TWO_KARMA);
		Assert.assertEquals("Checking first place " + user02.getId(), 1, service.getRank(leaderboardId, Leaderboard.TOTAL, user02.getId()));

		service.update(leaderboard.getId(), user03.getId(), Rewardable.THREE_KARMA);
		Assert.assertEquals("Checking first place " + user03.getId(), 1, service.getRank(leaderboardId, Leaderboard.TOTAL, user03.getId()));

		// Display leaderboard
		List<Score> descending = service.listDescending(leaderboardId, ScoreboardType.User, Leaderboard.TOTAL, 1, 10);
		Assert.assertEquals(3, descending.size());

		// Display leaderboard
		List<Score> ascending = service.listAscending(leaderboardId, ScoreboardType.User, Leaderboard.TOTAL, 3, 1);
		Assert.assertEquals(1, ascending.size());

		int count = 0;
		Map<String, List<LedgerEntry>> ledgerEntries = service.getLedger().getRecordEntries();
		for (List<LedgerEntry> list : ledgerEntries.values()) { count += list.size(); }
		Assert.assertEquals("Ledger entries", 3, count);
	}

	
	@Test
	public void register() {
		
		LeaderboardService service = Thinkr.INSTANCE.getLeaderboardService();
		Leaderboard leaderboard1 = service.create(title, Cycle.Weekly);
		String leaderboardId = leaderboard1.getId();
		
		Leaderboard leaderboard2 = service.getLeaderboard(leaderboardId);
		Assert.assertEquals(leaderboardId, leaderboard2.getId());
		
	}
	

	@Test
	public void generateReport() {
		
		Leaderboard leaderboard = createLeaderboard();
		Report generateReport = service.generateReport(leaderboard.getId(), ReportBuilderImpl.DEFAULT);
		Assert.assertEquals("Report entry size", 3, generateReport.getEntries().size());
		
	}
	
	private Leaderboard createLeaderboard() {
		Leaderboard leaderboard = service.create(title, Cycle.Weekly);
		
		String leaderboardId = leaderboard.getId();
		Assert.assertEquals(title, leaderboard.getTitle());
		Assert.assertEquals(leaderboard.getId(), leaderboardId);

		// Register 3 users
		service.addUser(leaderboardId, user01);
		service.addUser(leaderboardId, user02);
		service.addUser(leaderboardId, user03);
		Assert.assertEquals(3, leaderboard.getSize());
		
		service.update(leaderboard.getId(), user01.getId(), Rewardable.ONE_KARMA);
		service.update(leaderboard.getId(), user02.getId(), Rewardable.TWO_KARMA);
		service.update(leaderboard.getId(), user03.getId(), Rewardable.THREE_KARMA);
		
		return leaderboard;
	}
	
	@Test
	public void persistLeaderboard() {
		Leaderboard leaderboard = createLeaderboard();
		service.save(leaderboard, PersistenceMode.RedisLocal);
		Leaderboard loaded = service.load(leaderboard.getId(), PersistenceMode.RedisLocal);
		Assert.assertNotNull(loaded);
	}
	
	
	/**
	 * Create a monthly leaderboard. 
	 * Update 3 scores each day for a month
	 * Record the Ledger
	 * 
	 * Create a daily leaderboard
	 * Update leaderboard with ledger entries from monthly leaderboard
	 * verify that only 1 day of entries are added.
	 */
	@Test
	public void advancedFlow() {
		
		// Create a monthly leaderboard
		Leaderboard monthly = service.create("One Month Leaderboard", Cycle.Monthly);
		Assert.assertEquals("Monthly title", "One Month Leaderboard", monthly.getTitle());
		
		// Add 3 users
		service.addUser(monthly.getId(), user01);
		service.addUser(monthly.getId(), user02);
		service.addUser(monthly.getId(), user03);
		
		// Add 3 scores each day of the month
		OffsetDateTime now = OffsetDateTime.now();
		ZoneOffset localOffset = now.getOffset();		
		for (int day = 1; day <= 30; day++) {
			OffsetDateTime time = OffsetDateTime.of(now.getYear(), now.getMonthValue(), day, 0, 0, 0, 0, localOffset);
			service.update(monthly.getId(), user01.getId(), new Scorable(category, 1, time.plusHours(1)));
			service.update(monthly.getId(), user02.getId(), new Scorable(category, 2, time.plusHours(2)));
			service.update(monthly.getId(), user03.getId(), new Scorable(category, 3, time.plusHours(3)));
		}
		Assert.assertEquals("Monthly user01 score", 30, monthly.getScore(user01.getId(), category).getPoints());
		Assert.assertEquals("Monthly user02 score", 60, monthly.getScore(user02.getId(), category).getPoints());
		Assert.assertEquals("Monthly user03 score", 90, monthly.getScore(user03.getId(), category).getPoints());

		// Get ledger
		Ledger ledger = service.getLedger();
		
		// Create a daily leaderboard from ledger entries
		Leaderboard daily = service.create("One Day Leaderboard" , Cycle.Daily);
		service.update(daily.getId(), ledger.getRecordEntries());
		
		Assert.assertEquals("Daily user01 score", 1, daily.getScore(user01.getId(), category).getPoints());
		Assert.assertEquals("Daily user02 score", 2, daily.getScore(user02.getId(), category).getPoints());
		Assert.assertEquals("Daily user03 score", 3, daily.getScore(user03.getId(), category).getPoints());
	}
	
	
	/**
	 * Create a leaderboard set
	 * Update 3 scores each day for a month
	 * Record the Ledger
	 * 
	 * verify entries for each leaderboard.
	 * verify ledger entries
	 */
	@Test
	public void advancedFlowWithSet() {
		
		LeaderboardSet set = createLeaderboardSet();
		
		// Assert leaderboards
		validateAdvancedFlowLeaderboard(service.getLeaderboard(set.getLeaderboard(Cycle.Daily)), 1);
		validateAdvancedFlowLeaderboard(service.getLeaderboard(set.getLeaderboard(Cycle.Weekly)), 7);
		validateAdvancedFlowLeaderboard(service.getLeaderboard(set.getLeaderboard(Cycle.Monthly)), 30);

		// Check for overlapping ledger records resulting from 
		// multiple leaderboards {day, weekly, month}
		int count = 0;
		Ledger ledger = service.getLedger();
		Map<String, List<LedgerEntry>> recordEntries = ledger.getRecordEntries();
		for (String userId : recordEntries.keySet()) {
			Assert.assertEquals("Recordable per user", 30, recordEntries.get(userId).size());
			count += recordEntries.get(userId).size();
		}
		Assert.assertEquals("Recordable all users", 90, count);
	}
	
	
	private LeaderboardSet createLeaderboardSet() {
		// Create a leaderboard set
		LeaderboardSet set = service.createSet("AdvancedFlow", Cycle.Daily, Cycle.Weekly, Cycle.Monthly);
		
		// Add 3 users
		service.addUser(set.getId(), user01);
		service.addUser(set.getId(), user02);
		service.addUser(set.getId(), user03);
		
		// Add 3 scores each day of the month
		ZoneOffset localOffset = OffsetDateTime.now().getOffset();		
		for (int day = 1; day <= 30; day++) {
			OffsetDateTime time = OffsetDateTime.of(2018, 06, day, 0, 0, 0, 0, localOffset);
			service.update(set.getId(), user01.getId(), new Scorable(category, 1, time.plusHours(1)));
			service.update(set.getId(), user02.getId(), new Scorable(category, 2, time.plusHours(2)));
			service.update(set.getId(), user03.getId(), new Scorable(category, 3, time.plusHours(3)));
		}
		return set;
	}
	
	
	/**
	 * Validates the contents of a leaderboard from the advancedFlowLeaderboardSet test.
	 * @param leaderboard - the leaderboard
	 * @param days - the active duration in days
	 */
	private void validateAdvancedFlowLeaderboard(Leaderboard leaderboard, int days) {
		// Assert daily
		Assert.assertEquals(leaderboard.getCycle() + " user01 score", 1*days, leaderboard.getScore(user01.getId(), category).getPoints());
		Assert.assertEquals(leaderboard.getCycle() + " user02 score", 2*days, leaderboard.getScore(user02.getId(), category).getPoints());
		Assert.assertEquals(leaderboard.getCycle() + " user03 score", 3*days, leaderboard.getScore(user03.getId(), category).getPoints());

	}
	
	
	@Test
	public void persistable() {
		
		LeaderboardSet set = createLeaderboardSet();
		int ledgerEntryCount = 90;
		int leaderboardCount = 36;
		int leaderboardSetCount =1;
		Assert.assertTrue("LeaderboardService save", 
				service.save(PersistenceMode.RedisLocal, LeaderboardServiceSerializer.RESOURCE_ID));
		
		// Assert original state
		Map<String, LeaderboardImpl> leaderboards = Deencapsulation.getField(service, "leaderboards");
		Map<String, LeaderboardSetImpl> leaderboardSets = Deencapsulation.getField(service, "leaderboardSets");
		Assert.assertEquals("Service original - ledger", ledgerEntryCount, service.getLedger().getSize());
		Assert.assertEquals("Service original - leaderboards", leaderboardCount, leaderboards.keySet().size());
		Assert.assertEquals("Service original - leaderboardSets", leaderboardSetCount, leaderboardSets.keySet().size());

		// Reset service and validate
		Map<Service, ThinkrService> services = Deencapsulation.getField(Thinkr.INSTANCE, "services");
		services.remove(Service.Leaderboard);
		service = Thinkr.INSTANCE.getLeaderboardService();
		leaderboards = Deencapsulation.getField(service, "leaderboards");
		leaderboardSets = Deencapsulation.getField(service, "leaderboardSets");
		Assert.assertEquals("Service reset - ledger", 0, service.getLedger().getSize());
		Assert.assertEquals("Service reset - leaderboards", 0, leaderboards.keySet().size());
		Assert.assertEquals("Service reset - leaderboardSets", 0, leaderboardSets.keySet().size());
		
		// Load from redis and validate
		Assert.assertTrue("LeaderboardService load", 
				service.load(PersistenceMode.RedisLocal, LeaderboardServiceSerializer.RESOURCE_ID) != null);
		leaderboards = Deencapsulation.getField(service, "leaderboards");
		leaderboardSets = Deencapsulation.getField(service, "leaderboardSets");
		Assert.assertEquals("Service loaded - ledger", ledgerEntryCount, service.getLedger().getSize());
		Assert.assertEquals("Service loaded - leaderboards", leaderboardCount, leaderboards.keySet().size());
		Assert.assertEquals("Service loaded - leaderboardSets", leaderboardSetCount, leaderboardSets.keySet().size());
		
		Leaderboard daily = service.getLeaderboard(set.getLeaderboard(Cycle.Daily));
		validateAdvancedFlowLeaderboard(daily, 1);
		
		Leaderboard weekly = service.getLeaderboard(set.getLeaderboard(Cycle.Weekly));
		validateAdvancedFlowLeaderboard(weekly, 7);
		
		Leaderboard monthly = service.getLeaderboard(set.getLeaderboard(Cycle.Monthly));
		validateAdvancedFlowLeaderboard(monthly, 30);
	}
}
