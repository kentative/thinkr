package com.bytes.fmk.service.leaderboard.impl;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import mockit.Deencapsulation;

// TODO Leaderboard serialization tests
public class LeaderboardSerializerTest {

	
	private String leaderboardJson;
	private OffsetDateTime start;
	private OffsetDateTime end;
	
	@Before
	public void setup() {
		leaderboardJson="{\"id\":\"f7a2896b-adf2-4f3d-a6d6-898385dd305d\",\"scoreboards\":{\"User\":{\"type\":\"User\",\"scoreEntries\":{},\"categories\":{\"Total\":{\"name\":\"Total\",\"isStaled\":true}},\"title\":\"User\"},\"Guild\":{\"type\":\"Guild\",\"scoreEntries\":{},\"categories\":{\"Total\":{\"name\":\"Total\",\"isStaled\":true}},\"title\":\"Guild\"},\"Team\":{\"type\":\"Team\",\"scoreEntries\":{},\"categories\":{\"Total\":{\"name\":\"Total\",\"isStaled\":true}},\"title\":\"Team\"}},\"title\":\"Leaderboard\",\"startDate\":\"2018-05-22T10:12:37Z\",\"endDate\":\"2018-05-27T10:12:37Z\",\"isActive\":false,\"autoUpdate\":true,\"autoPersist\":false,\"cycle\":\"Weekly\"}";
		start = OffsetDateTime.of(2018, 5, 22, 10, 12, 37, 0, ZoneOffset.UTC);
		end = OffsetDateTime.of(2018, 5, 27, 10, 12, 37, 0, ZoneOffset.UTC);
	}

	
	
	@Test
	public void serializeLeaderboard() {
		
		LeaderboardImpl leaderboard = new LeaderboardImpl();
		Deencapsulation.setField(leaderboard, "id", "f7a2896b-adf2-4f3d-a6d6-898385dd305d");
		leaderboard.setStartDate(start);
		leaderboard.setEndDate(end);
	
		// A weak verification
		Assert.assertTrue("leaderboard in JSON format", LeaderboardSerializer.toJson(leaderboard).length() > 100);
		
	}
	
	@Test
	public void deserializeLeaderboard() {
		
		LeaderboardSerializer serializer = new LeaderboardSerializer();
		serializer.registerLeaderboard("1");
		LeaderboardImpl leaderboard = serializer.getData(leaderboardJson, "1");
		Assert.assertEquals("f7a2896b-adf2-4f3d-a6d6-898385dd305d", leaderboard.getId());
		Assert.assertEquals(start, leaderboard.getStartDate());
		Assert.assertEquals(end, leaderboard.getEndDate());
		Assert.assertEquals(1, leaderboard.getCategoryNames().size());
		
	}
}
