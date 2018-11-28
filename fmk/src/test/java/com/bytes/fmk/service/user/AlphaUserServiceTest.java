package com.bytes.fmk.service.user;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.bytes.fmk.data.model.Guild;
import com.bytes.fmk.data.model.Team;
import com.bytes.fmk.service.Thinkr;
import com.bytes.fmk.service.persistence.PersistenceMode;
import com.bytes.fmk.service.persistence.Serializer;
import com.bytes.fmk.service.user.impl.UserServiceImpl;
import com.google.gson.reflect.TypeToken;

import mockit.Deencapsulation;


public class AlphaUserServiceTest {
	
	private String resourceId = "AlphaUserResourceID";
	
	private List<AlphaUser> users;
	
	@Before
	public void setup() {
	
		Guild guild = new Guild("Ascalon");
		Team teamEven = new Team("Even");
		Team teamOdd = new Team("Odd");
		teamEven.addGuild(guild);
		teamOdd.addGuild(guild);
		
		users = new ArrayList<>();
		for (int i = 0; i < 50; i++) {
			AlphaUser user = new AlphaUser(UUID.randomUUID().toString(), "User"+i );
			user.setFriendshipCode("code" + i);
			if (i %2 == 0) {
				user.addTeam(teamEven);
			} else {
				user.addTeam(teamOdd);
			}
			users.add(user);
		}
	}
	
	@Test
	public void register() {

		UserService<AlphaUser> service = Thinkr.INSTANCE.getUserService();
		for (AlphaUser user : users) {
			user.setFriendshipCode("code"+user.getDisplayName());
			service.registerUser(user.getId(), user);
		}
		
		for (AlphaUser user : users) {
			Assert.assertEquals("Matching registered display name", 
					user.getDisplayName(), 
					service.getUser(user.getId()).getDisplayName());
			
			Assert.assertEquals("FriendCode", 
					"code"+user.getDisplayName(), 
					user.getFriendshipCode());
		}
	}
	
	
	@Test
	public void unregister() {
		UserService<AlphaUser> service = Thinkr.INSTANCE.getUserService();
		for (AlphaUser user : users) {
			service.registerUser(user.getId(), user);
		}
		
		for (AlphaUser user : users) {
			service.unregisterUser(user.getId());
			Assert.assertNull(service.getUser(user.getId()));
		}
	}
	
	@Test
	public void persistence() {
		
		UserService<AlphaUser> service = Thinkr.INSTANCE.getUserService();
		AlphaUser user1 = users.get(0);
		AlphaUser user2 = users.get(1);
		AlphaUser user3 = users.get(2);
		
		service.registerUser(user1.getId(), user1);
		service.registerUser(user2.getId(), user2);
		service.registerUser(user3.getId(), user3);
		
		Serializer serializer = service.getSerializer();
		serializer.registerResourceID(
				resourceId,
				new TypeToken<UserServiceImpl<AlphaUser>>(){}.getType());
		
		service.save(PersistenceMode.RedisLocal, resourceId);
		Deencapsulation.setField(service, "registeredUsers", new ConcurrentHashMap<>());
		Assert.assertEquals("Clear existing service data before loadingdata", service.getUsers().size(), 0);
		
		service.load(PersistenceMode.RedisLocal, resourceId);
		Assert.assertEquals("Checking size", 3, service.getUsers().size());
		
		Assert.assertEquals("Checking user1", 
				user1.getFriendshipCode(),
				service.getUser(user1.getId()).getFriendshipCode());
		
		Assert.assertEquals("Checking user2", 
				user2.getFriendshipCode(),
				service.getUser(user2.getId()).getFriendshipCode());
		
		Assert.assertEquals("Checking user3", 
				user3.getFriendshipCode(),
				service.getUser(user3.getId()).getFriendshipCode());
		
	}
	
	@Test
	public void serialize() {
		
		UserService<AlphaUser> service = Thinkr.INSTANCE.getUserService();
		service.getSerializer().registerResourceID(
				resourceId, 
				new TypeToken<UserServiceImpl<AlphaUser>>(){}.getType());
		
		AlphaUser user1 = users.get(0);
		AlphaUser user2 = users.get(1);
		AlphaUser user3 = users.get(2);
		
		service.registerUser(user1.getId(), user1);
		service.registerUser(user2.getId(), user2);
		service.registerUser(user3.getId(), user3);
		
		String json = Serializer.toJson(service);
		UserService<AlphaUser> data = service.getSerializer().getData(json, resourceId);
		AlphaUser user = data.getUser(user1.getId());
		Assert.assertEquals("code0", user.getFriendshipCode());
		Assert.assertEquals(
				service.getUsers().size(), 
				data.getUsers().size());
		
	}
}
