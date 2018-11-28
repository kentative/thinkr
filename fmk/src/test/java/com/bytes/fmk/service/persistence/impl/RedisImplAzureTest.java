package com.bytes.fmk.service.persistence.impl;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.bytes.fmk.data.model.User;
import com.bytes.fmk.service.persistence.DefaultSerializer;
import com.bytes.fmk.service.persistence.impl.RedisImpl;

import redis.clients.jedis.Jedis;


public class RedisImplAzureTest {

	private Map<String, User> userMap;
	
	@Before
	public void setup() {
		userMap = new HashMap<>();
		userMap.put("1", new User("Kent"));
		userMap.put("2", new User("Kydan"));
		userMap.put("3", new User("Kaelyn"));
		
		// ***WARNING*** This will wipe all data
//		RedisImpl redisService = RedisImpl.AzureThinkr;
//		Jedis jedis = redisService.getPoolInstance().getResource();
//		jedis.flushAll();
	}
	
	@Test
	public void getPool() {
		
		RedisImpl redisService = RedisImpl.AzureThinkr;
		Jedis jedis = redisService.getPoolInstance().getResource();
		String key = "key123";
		String value = "value123";
		jedis.set(key, value);
		
		Assert.assertEquals(value, jedis.get(key));
		jedis.close();
	}
	
	@Test
	public void saveAndLoadMap() {
		
		RedisImpl redisService = RedisImpl.AzureThinkr;
		String resourceId = "User";
		redisService.saveMap(resourceId, userMap);
		DefaultSerializer serializer = new DefaultSerializer();
		
		Map<String, User> response = redisService.loadMap(resourceId, serializer);
		
		Assert.assertEquals(userMap.get("1").getDisplayName(), response.get("1").getDisplayName());
		Assert.assertEquals(userMap.get("2").getDisplayName(), response.get("2").getDisplayName());
		Assert.assertEquals(userMap.get("3").getDisplayName(), response.get("3").getDisplayName());
		
	}

}
