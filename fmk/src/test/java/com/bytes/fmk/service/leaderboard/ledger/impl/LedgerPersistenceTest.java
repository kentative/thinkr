package com.bytes.fmk.service.leaderboard.ledger.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.bytes.fmk.service.leaderboard.Scorable;
import com.bytes.fmk.service.leaderboard.ledger.Ledger;
import com.bytes.fmk.service.leaderboard.ledger.LedgerTest;
import com.bytes.fmk.service.persistence.PersistenceMode;
import com.bytes.fmk.service.persistence.Serializer;

import mockit.Deencapsulation;


public class LedgerPersistenceTest extends LedgerTest {
	
	private String record1;
	private String record2;
	
	private String user1;
	private String user2;
	private String user3;

	private Scorable scorable1;
	private Scorable scorable2 ;
	private Scorable scorable3;
	
	private Ledger ledger;
	
	@Before
	public void setup() {
		record1 = "record1";
		record2 = "record2";
		
		user1 = "user1";
		user2 = "user2";
		user3 = "user3";

		scorable1 = new Scorable("cat1", 3);
		scorable2 = new Scorable("cat2", 4);
		scorable3 = new Scorable("cat3", 5);
		
		ledger = new LedgerImpl();
		int count = 3;
		for (int i = 0; i < count; i++) {
			ledger.add(record1, user1, scorable1);
			ledger.add(record1, user2, scorable2);
			ledger.add(record1, user3, scorable3);
			
			ledger.add(record2, user1, scorable1);
			ledger.add(record2, user2, scorable2);
			ledger.add(record2, user3, scorable3);
		}
	}
	
	
	@Test
	public void serialize() throws Exception {
		String json = Serializer.toJson(ledger);
		Assert.assertNotNull(json);
	}
	

	@Test
	public void deserialize() throws Exception {
		
		Serializer serializer = ledger.getSerializer();
		String json = Serializer.toJson(ledger);
		Ledger deserialized = Serializer.fromJson(json, LedgerImpl.RESOURCE_ID, serializer);
		
		Assert.assertEquals("deserialized instance", ledger.getId(), deserialized.getId());
		validateUserEntries(3, scorable1.getCategoryName(), deserialized.getUserEntries(record1, user1));
	}
	
	
	@Test
	public void saveAndLoad() {
		
		Assert.assertTrue("Local persistence", 
				ledger.save(PersistenceMode.RedisLocal, LedgerSerializer.RESOURCE_ID));
		
		Deencapsulation.setField(ledger, "id", null);
		Deencapsulation.setField(ledger, "entries", null);
		Deencapsulation.setField(ledger, "recordables", null);
		Assert.assertTrue("Nulled instanced for loading", ledger.getId() == null);
		
		ledger.load(PersistenceMode.RedisLocal, LedgerSerializer.RESOURCE_ID);
		validateUserEntries(3, scorable1.getCategoryName(), ledger.getUserEntries(record1, user1));
	}
}
