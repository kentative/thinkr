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
import com.bytes.fmk.data.model.User;
import com.bytes.fmk.service.Thinkr;
import com.bytes.fmk.service.persistence.PersistenceMode;
import com.bytes.fmk.service.persistence.Serializer;
import com.bytes.fmk.service.user.impl.UserServiceSerializer;

import mockit.Deencapsulation;


public class UserServiceTest {
	
	
	private List<User> users;
	
	@Before
	public void setup() {
	
		Guild guild = new Guild("Ascalon");
		Team teamEven = new Team("Even");
		Team teamOdd = new Team("Odd");
		teamEven.addGuild(guild);
		teamOdd.addGuild(guild);
		
		users = new ArrayList<>();
		for (int i = 0; i < 50; i++) {
			User user = new User(UUID.randomUUID().toString(), "User"+i );
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

		UserService<User> service = Thinkr.INSTANCE.getUserService();
		for (User user : users) {
			service.registerUser(user.getId(), user);
		}
		
		for (User user : users) {
			Assert.assertEquals("Matching registered display name", 
					user.getDisplayName(), service.getUser(user.getId()).getDisplayName());
		}
	}
	
	
	@Test
	public void unregister() {
		UserService<User> service = Thinkr.INSTANCE.getUserService();
		for (User user : users) {
			service.registerUser(user.getId(), user);
		}
		
		for (User user : users) {
			service.unregisterUser(user.getId());
			Assert.assertNull(service.getUser(user.getId()));
		}
	}
	
	@Test
	public void persistence() {
		
		UserService<User> service = Thinkr.INSTANCE.getUserService();
		User user1 = users.get(0);
		User user2 = users.get(1);
		User user3 = users.get(2);
		
		service.registerUser(user1.getId(), user1);
		service.registerUser(user2.getId(), user2);
		service.registerUser(user3.getId(), user3);
		
		service.save(PersistenceMode.RedisLocal, UserServiceSerializer.RESOURCE_ID);
		
		Deencapsulation.setField(service, "registeredUsers", new ConcurrentHashMap<>());
		Assert.assertEquals("Clear existing service data before loadingdata", service.getUsers().size(), 0);
		
		service.load(PersistenceMode.RedisLocal, UserServiceSerializer.RESOURCE_ID);
		Assert.assertEquals("Checking size", 3, service.getUsers().size());
		Assert.assertEquals("Checking user1", 
				user1, 
				service.getUser(user1.getId()));
		
		Assert.assertEquals("Checking user2", 
				user2, 
				service.getUser(user2.getId()));
		
		Assert.assertEquals("Checking user3", 
				user3, 
				service.getUser(user3.getId()));
		
	}
	
	@Test
	public void serialize() {
		
		UserService<User> service = Thinkr.INSTANCE.getUserService();
		User user1 = users.get(0);
		User user2 = users.get(1);
		User user3 = users.get(2);
		
		service.registerUser(user1.getId(), user1);
		service.registerUser(user2.getId(), user2);
		service.registerUser(user3.getId(), user3);
		
		String json = Serializer.toJson(service);
		UserService<User> data = service.getSerializer().getData(json, UserServiceSerializer.RESOURCE_ID);
		Assert.assertEquals(
				service.getUsers().size(), 
				data.getUsers().size());
		
	}
}
