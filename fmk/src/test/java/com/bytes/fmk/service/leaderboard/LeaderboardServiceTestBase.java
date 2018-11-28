package com.bytes.fmk.service.leaderboard;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Before;

import com.bytes.fmk.data.model.Guild;
import com.bytes.fmk.data.model.Team;
import com.bytes.fmk.data.model.User;
import com.bytes.fmk.service.Thinkr;
import com.bytes.fmk.service.user.UserService;

import mockit.Deencapsulation;

public class LeaderboardServiceTestBase {

	protected User user01;
	protected User user02;
	protected User user03;
	protected LeaderboardService service;
	
	@Before
	public void setup() {

		Guild guild = new Guild("Guild");
		user01 = new User(UUID.randomUUID().toString(), "Kent", new Team("Team1"), guild);
		user02 = new User(UUID.randomUUID().toString(), "Kydan", new Team("Team2"), guild);
		user03 = new User(UUID.randomUUID().toString(), "Kaelyn", new Team("Team3"), guild);
		
		UserService<User> userService = Thinkr.INSTANCE.getUserService();
		userService.registerUser(user01.getId(), user01);
		userService.registerUser(user02.getId(), user02);
		userService.registerUser(user03.getId(), user03);
		
		// Reset service
		Deencapsulation.setField(
				Thinkr.INSTANCE, 
				"services", new ConcurrentHashMap<>());
		
		service = Thinkr.INSTANCE.getLeaderboardService();
	}
}
