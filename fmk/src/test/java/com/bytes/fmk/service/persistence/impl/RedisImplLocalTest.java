package com.bytes.fmk.service.persistence.impl;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.bytes.fmk.data.model.User;
import com.bytes.fmk.service.persistence.DefaultSerializer;
import com.google.gson.reflect.TypeToken;

import redis.clients.jedis.Jedis;


public class RedisImplLocalTest {

	private Map<String, User> userMap;
	
	@Before
	public void setup() {
		userMap = new HashMap<>();
		userMap.put("1", new User("Kent"));
		userMap.put("2", new User("Kydan"));
		userMap.put("3", new User("Kaelyn"));
		
		RedisImpl redisService = RedisImpl.Local;
		Jedis jedis = redisService.getPoolInstance().getResource();
		jedis.flushAll();
	}
	
	@Test
	public void getPoolLocal() {
		
		RedisImpl redisService = RedisImpl.Local;
		Jedis jedis = redisService.getPoolInstance().getResource();
		String key = "key123";
		String value = "value123";
		jedis.set(key, value);
		
		Assert.assertEquals(value, jedis.get(key));
		jedis.close();
	}
	
	@Test
	public void saveAndLoadMapLocal() {
		
		RedisImpl redisService = RedisImpl.Local;
		String resourceId = "User";
		redisService.saveMap(resourceId, userMap);
		DefaultSerializer serializer = new DefaultSerializer();
		
		Map<String, User> response = redisService.loadMap(resourceId, serializer);
		Assert.assertEquals(userMap.get("1").getDisplayName(), response.get("1").getDisplayName());
		Assert.assertEquals(userMap.get("2").getDisplayName(), response.get("2").getDisplayName());
		Assert.assertEquals(userMap.get("3").getDisplayName(), response.get("3").getDisplayName());
		
	}
	
	@Test
	public void getPoolUsage() {
		
		RedisImpl redisService = RedisImpl.Local;
		System.out.println(redisService.getPoolCurrentUsage());
		
	} 
	
	@Test
	public void clearResourceId() {
		
		String resourceId = "User";
		RedisImpl redisService = RedisImpl.Local;

		Assert.assertFalse("Empty cache - should return false", 
				redisService.clear(resourceId));
		
		redisService.saveMap(resourceId, userMap);
		redisService = RedisImpl.Local;
		Assert.assertTrue("Non empty cache - should return true",
				redisService.clear(resourceId));
		
		DefaultSerializer serializer = new DefaultSerializer();
		Map<String, User> response = redisService.loadMap(resourceId, serializer);
		Assert.assertEquals("Cache clear - expects 0 entries", 0, response.size());
	}
	
	@Test
	public void saveAndLoadDataLocal() {
		
		RedisImpl redisService = RedisImpl.Local;
		String resourceId = "testKey";
		
		User data = new User("Test User");
		redisService.saveData(resourceId, data);
		
		DefaultSerializer serializer = new DefaultSerializer();
		serializer.addMapping(resourceId, new TypeToken<User>(){}.getType());
		User response = redisService.loadData(resourceId, serializer);
		Assert.assertEquals(data.getDisplayName(), response.getDisplayName());
	}

}
