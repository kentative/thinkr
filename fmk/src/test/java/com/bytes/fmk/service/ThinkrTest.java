package com.bytes.fmk.service;

import org.junit.Assert;
import org.junit.Test;

import com.bytes.fmk.service.leaderboard.LeaderboardService;
import com.bytes.fmk.service.persistence.PersistenceService;
import com.bytes.fmk.service.user.UserService;


public class ThinkrTest {

	@Test
	public void persistenceService() {
		PersistenceService service = Thinkr.INSTANCE.getPersistenceService();
		Assert.assertNotNull(service);
	}
	
	@Test
	public void leaderboardService() {
		LeaderboardService service = Thinkr.INSTANCE.getLeaderboardService();
		Assert.assertNotNull(service);
		
	}
	
	@Test
	public void userService() {
		UserService service = Thinkr.INSTANCE.getUserService();
		Assert.assertNotNull(service);
		
	}
}
