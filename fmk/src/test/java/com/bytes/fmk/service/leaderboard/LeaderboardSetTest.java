package com.bytes.fmk.service.leaderboard;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.bytes.fmk.service.Thinkr;
import com.bytes.fmk.service.leaderboard.impl.LeaderboardSetSerializer;
import com.bytes.fmk.service.persistence.PersistenceMode;
import com.bytes.fmk.service.persistence.Serializer;

import mockit.Deencapsulation;


public class LeaderboardSetTest {

	private LeaderboardSet set;
	private String title;
	private LeaderboardService service;
	
	@Before
	public void setup() {
		
		title = "Test Leaderboard";
		service = Thinkr.INSTANCE.getLeaderboardService();
		set = service.createSet(title, Cycle.Daily, Cycle.Weekly, Cycle.Monthly);
	}
	
	
	@Test
	public void getLeaderboardByCycle() {
		
		Assert.assertEquals("Daily cycle", Cycle.Daily, 
				service.getLeaderboard(set.getLeaderboard(Cycle.Daily)).getCycle());
		
		Assert.assertEquals("Weekly cycle", Cycle.Weekly, 
				service.getLeaderboard(set.getLeaderboard(Cycle.Weekly)).getCycle());
		
		Assert.assertEquals("Monthly cycle", Cycle.Monthly, 
				service.getLeaderboard(set.getLeaderboard(Cycle.Monthly)).getCycle());

	}
	
	@Test
	public void getLeaderboardId() {
		
		Leaderboard daily = service.getLeaderboard(set.getLeaderboard(Cycle.Daily));
		Assert.assertEquals("Daily leaderboard set ID", 
				getId(daily, title), 
				set.generateLeaderboardId(daily));
		Assert.assertEquals("Daily leaderboard set ID linked", 
				getId(daily, title), 
				daily.getId());
		
		Leaderboard weekly = service.getLeaderboard(set.getLeaderboard(Cycle.Weekly));
		Assert.assertEquals("Weekly leaderboard set ID", 
				getId(weekly, title), 
				set.generateLeaderboardId(weekly));
		Assert.assertEquals("Weekly leaderboard set ID linked", 
				getId(weekly, title), 
				weekly.getId());
		
		Leaderboard monthly = service.getLeaderboard(set.getLeaderboard(Cycle.Monthly));
		Assert.assertEquals("Monthly leaderboard set ID", 
				getId(monthly, title), 
				set.generateLeaderboardId(monthly));
		Assert.assertEquals("Monthly leaderboard set ID linked", 
				getId(monthly, title), 
				monthly.getId());
	}
	
	
	@Test
	public void serialize() {
		
		String json = Serializer.toJson(set);
		LeaderboardSet data = set.getSerializer().getData(json, LeaderboardSetSerializer.RESOURCE_ID);
		Assert.assertEquals(
				set.getLeaderboard(Cycle.Daily), 
				data.getLeaderboard(Cycle.Daily));
		
	}
	
	@Test
	public void persistence() {
		
		set.save(PersistenceMode.RedisLocal, LeaderboardSetSerializer.RESOURCE_ID);
		String dailyId = set.getLeaderboard(Cycle.Daily);
		String weeklyId = set.getLeaderboard(Cycle.Weekly);
		String monthlyId = set.getLeaderboard(Cycle.Monthly);
		
		Deencapsulation.setField(set, "leaderboardIds", new ConcurrentHashMap<>());
		Assert.assertEquals("Clear existing data before loading", set.getAll().size(), 0);
		
		set.load(PersistenceMode.RedisLocal, LeaderboardSetSerializer.RESOURCE_ID);
		Assert.assertEquals("Checking size", 3, set.getAll().size());
		
		Assert.assertEquals("Checking dailyId", 
				dailyId,
				set.getLeaderboard(Cycle.Daily));
		
		Assert.assertEquals("Checking weeklyId", 
				weeklyId,
				set.getLeaderboard(Cycle.Weekly));
		
		Assert.assertEquals("Checking monthlyId", 
				monthlyId,
				set.getLeaderboard(Cycle.Monthly));
	}
	
	
	@Test
	public void save() {
		
	}
	
	
	@Test
	public void load() {
		
	}
	
	
	/**
	 * Get the leaderboard id based on a structured format:
	 * title + cycle + day leaderboard of start time in ISO_DATE format
	 * @param leaderboard the leaderboard
	 * @param title the leaderboard title
	 * @return the structured id - can be use to lookup in persistence store
	 */
	private String getId(Leaderboard leaderboard, String title) {
		return title + "_" + 
				leaderboard.getCycle() + "_" +
				leaderboard.getStartDate().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
	}
	
	
}
