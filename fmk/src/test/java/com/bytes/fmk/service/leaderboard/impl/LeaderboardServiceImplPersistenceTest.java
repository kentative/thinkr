package com.bytes.fmk.service.leaderboard.impl;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.bytes.fmk.data.model.User;
import com.bytes.fmk.service.Thinkr;
import com.bytes.fmk.service.Thinkr.Service;
import com.bytes.fmk.service.ThinkrService;
import com.bytes.fmk.service.leaderboard.Cycle;
import com.bytes.fmk.service.leaderboard.Scorable;
import com.bytes.fmk.service.leaderboard.Score;
import com.bytes.fmk.service.leaderboard.ScoreboardType;
import com.bytes.fmk.service.leaderboard.ledger.Ledger;
import com.bytes.fmk.service.leaderboard.ledger.impl.RecordableScore;
import com.bytes.fmk.service.leaderboard.report.Report;
import com.bytes.fmk.service.leaderboard.report.ReportBuilder;
import com.bytes.fmk.service.leaderboard.report.impl.ReportBuilderImpl;
import com.bytes.fmk.service.persistence.PersistenceMode;

import mockit.Deencapsulation;


public class LeaderboardServiceImplPersistenceTest {
	
	private static final int MAX_USERS = 50;
	private String categoryName = "Karma";
	private String leaderboardTitle = "Thinkr Leaderboard";
	
	private LeaderboardServiceImpl service;
	private String leaderboardId = "Test leaderboard";
	private LeaderboardImpl leaderboard;
	
	/**
	 */
	@Before
	public void setup() {
		service = new LeaderboardServiceImpl();
		leaderboard = service.create(leaderboardTitle, Cycle.Weekly);
		leaderboard.addCategories(categoryName);
		leaderboardId = leaderboard.getId();
		Deencapsulation.setField(leaderboard, "id", leaderboardId);
		// re-register to update ID
		service.register(leaderboard);
		
		// Allow usage of LeaderboardServiceImpl with the public facing API
		Map<Service, ThinkrService> services = new HashMap<>();
		services.put(Thinkr.Service.Leaderboard, service);
		Deencapsulation.setField(Thinkr.INSTANCE, "services", services);
	}

	
	/**
	 * Clone the current leaderboard and test for equality.
	 * ***Warning*** This test wipes data, run only in local mode
	 * <pre>
	 * start server:
	 * 		src/redis-server 
	 * 	
	 * start client:
	 * 		src/redis-cli 
	 * 		auth password
	 * 		hgetall leaderboardId
	 * </pre>
	 */
	@Test
	public void saveAndLoadLeaderboard() {
		
		// Register 50 users and generate scores
		for (int i = 0; i < MAX_USERS; i++) {
			String username = "userId"+(i+1);
			User user = new User(username, username+"_Name");
			service.addUser(leaderboardId, user);
			Scorable scoreable = new Scorable(categoryName, i*2);
			service.update(leaderboardId, username, scoreable);
		}
		service.calculate(leaderboardId);
		
		// 1. Clear Cache
//		PersistenceService cache = RedisImpl.Local;
//		leaderboardId = leaderboard.getId();
//		cache.clear(leaderboardId);
		
		// 2. Save current leaderboard to Cache
		Assert.assertTrue(service.save(leaderboard, PersistenceMode.RedisLocal));
		
		// 3. Load - Create a new leaderboard instance and load from Cache
		LeaderboardImpl leaderboard2 = service.load(leaderboardId, PersistenceMode.RedisLocal);
		Assert.assertEquals(leaderboardId, leaderboard2.getId());
		
		// 4. Compare strongly-type map between the original and new leaderboard
		List<Score> expectedScores = service.listDescending(leaderboardId, 1, MAX_USERS);
		List<Score> actualScores = service.listDescending(leaderboard2.getId(), 1, MAX_USERS);
		
		Assert.assertEquals(expectedScores.size(), actualScores.size());
		Assert.assertEquals(MAX_USERS, actualScores.size());
		for (int i = 0; i < expectedScores.size(); i++) {
			Assert.assertEquals(expectedScores.get(i), actualScores.get(i));
		}
	}
	
	
	@Test
	public void merge() {
		
		String sourceTitle = "Source Leaderboard";
		String targetTitle = "Target Leaderboard";
		String[] sourceCategories = new String[]{"CategoryS1", "CategoryS2", "CategoryST1"};
		String[] targetCategories = new String[]{"CategoryT1", "CategoryT2", "CategoryST1"};

		// Generate source leaderboard and persists
		LeaderboardImpl source = create(sourceTitle, sourceCategories, 1, 10);
		LeaderboardImpl target = create(targetTitle, targetCategories, 5, 15);

		Assert.assertTrue(service.save(source, PersistenceMode.RedisLocal));
		Assert.assertTrue(service.save(target, PersistenceMode.RedisLocal));
		
		// Merge
		LeaderboardImpl merged = service.merge(
				source.getId(), PersistenceMode.RedisLocal, 
				target.getId(), PersistenceMode.RedisLocal);
		
		Assert.assertEquals("Leaderboard Title", target.getTitle(), merged.getTitle());
		Assert.assertEquals("Number of user", 15, merged.getSize());
		Assert.assertEquals("Number of categories, including Total", 6, merged.getCategoryNames().size());
		
		service.calculate(merged.getId());
		List<Score> mergedScores = service.listDescending(merged.getId(), ScoreboardType.User, targetCategories[2], 1, 100);
		
		Assert.assertEquals("Merged category", targetCategories[2], mergedScores.get(0).getCategoryName());
		Assert.assertEquals("Bottom rank score for merged leaderboard", 1, merged.getScore("userId1", targetCategories[2]).getPoints());
		Assert.assertEquals("Top rank score for merged leaderboard", 20, merged.getScore("userId10", targetCategories[2]).getPoints());
		mergedScores.forEach(x -> System.out.println(x.getCategoryName() + " " + x.getEntryId() + " " + x.getPoints()));
		
		
		// Verify the integrity of the merged leaderboard
		System.out.println(service.generateReport(merged.getId(), ReportBuilderImpl.DEFAULT));
		service.save(merged, PersistenceMode.RedisLocal);
		System.out.println(merged.getId());
	}
	
	@Test
	public void saveLedger() {
		
		String userIdPrefix = "userId";
		// Register 50 users and generate scores
		for (int i = 0; i < MAX_USERS; i++) {
			User user = new User(userIdPrefix + (i+1));
			service.addUser(leaderboardId, user);
			service.update(leaderboardId, userIdPrefix+(i+1), RecordableScore.Point);
		}
		
		String userId = userIdPrefix+1;
		Ledger ledger = service.getLedger();
		service.update(leaderboard.getId(), userId, RecordableScore.Point);
		Assert.assertEquals(51, ledger.getSize());
		Assert.assertEquals(2, ledger.getRecordEntries().get(userId).size());
		
		service.update(leaderboard.getId(), userId, RecordableScore.Point);
		Assert.assertEquals("Added another update",
				3, ledger.getRecordEntries().get(userId).size());
		
		service.calculate(leaderboardId);
		Report generateReport = service.generateReport(leaderboardId, ReportBuilder.DEFAULT);
		System.out.println(generateReport);
		
		service.save(leaderboard, PersistenceMode.RedisLocal);
		
	}
	
	
	/**
	 * TODO might not be needed, should be part of load
	 */
	@Test
	@Ignore 
	public void checkIntegrity() {
		
		LeaderboardImpl loaded = service.load(leaderboardId, PersistenceMode.RedisLocal);
		
		Scorable scoreable1 = new Scorable("CategoryST1", 5);
		service.update(loaded.getId(), "userId10", scoreable1);
		System.out.println(service.generateReport(loaded.getId(), ReportBuilderImpl.DEFAULT));
		
		Scorable scoreable2 = new Scorable("CategoryST1", 15);
		service.update(loaded.getId(), "userId10", scoreable2);
		System.out.println(service.generateReport(loaded.getId(), ReportBuilderImpl.DEFAULT));
		
	}
	

	private LeaderboardImpl create(String title, String[] categories, int userIdStart, int userIdEnd) {
		
		// Generate target leaderboard and persists
		LeaderboardImpl created = service.create(title, Cycle.Daily);
		created.addCategories(categories);
		for (int i = userIdStart; i <= userIdEnd; i++) {
			service.addUser(created.getId(), new User("userId"+(i)));
			service.update(created.getId(), "userId"+(i), new Scorable(categories[0], i));
			service.update(created.getId(), "userId"+(i), new Scorable(categories[1], i));
			service.update(created.getId(), "userId"+(i), new Scorable(categories[2], i));
		}
		return created;
	}
	
	@Test
	public void persistenceMode() {
		String input = "azure";
		PersistenceMode mode = PersistenceMode.RedisLocal;
		try {
			mode = Enum.valueOf(PersistenceMode.class, input);
		} catch (Exception e) {
			
		}
		if (mode == null) {
			if (input.equals("local")) mode = PersistenceMode.RedisLocal;
			if (input.equals("azure")) mode = PersistenceMode.RedisAzure;
		}		
	}
	
	/**
	 * Generate a leaderboard report using data from the local Redis instance.
	 * - need to save one first
	 */
	@Test
	@Ignore 
	public void reportLocal() {
		String localLeaderboardId = getLocalLeaderboardId();
		service.load(localLeaderboardId, PersistenceMode.RedisLocal);
		System.out.println(service.generateReport(localLeaderboardId, ReportBuilder.DEFAULT.setMaxEntries(200)));
	}

	
	/**
	 * Generate the local leaderboard id based on the LEADERBOARD_ID. To prevent overwriting good data, 
	 * 7 versions are create, one per day of week. If there is corruption, use the previous day data.
	 * @return the leaderboard id (the redis resourceId) to load from the local Redis instance
	 */
	private String getLocalLeaderboardId() {
		return "d6f3ec3c-8932-4a7c-bac2-9812a9de4a84" + "_" + LocalDate.now().getDayOfWeek();
	}
}
