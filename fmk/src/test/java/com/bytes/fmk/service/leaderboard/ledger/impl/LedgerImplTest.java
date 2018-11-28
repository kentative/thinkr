package com.bytes.fmk.service.leaderboard.ledger.impl;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.bytes.fmk.service.leaderboard.Scorable;
import com.bytes.fmk.service.leaderboard.ledger.Ledger;
import com.bytes.fmk.service.leaderboard.ledger.LedgerEntry;
import com.bytes.fmk.service.leaderboard.ledger.LedgerTest;


public class LedgerImplTest extends LedgerTest {
	
	private String record1;
	private String record2;
	
	private String user1;
	private String user2;
	private String user3;

	private String category1 = "Cat1";
	private String category2 = "Cat2";
	private String category3 = "Cat3";
	
	private Ledger ledger;
	
	@Before
	public void setup() {
		record1 = "record1";
		record2 = "record2";
		
		user1 = "user1";
		user2 = "user2";
		user3 = "user3";

		ledger = new LedgerImpl();
	}
	
	
	@Test
	public void addEntries() throws Exception {
		
		int count = 3;
		OffsetDateTime now = OffsetDateTime.now();
		for (int i = 0; i < count; i++) {
			ledger.add(record1, user1, new Scorable(category1, 3, now));
		}
		
		List<LedgerEntry> user1Entries = ledger.getUserEntries(record1, user1);
		Assert.assertEquals("Add - multiple entries same record, same time",  
				1, user1Entries.size());
		
		for (int i = 0; i < count; i++) {
			ledger.add(record1, user2, new Scorable(category1, 3, now.plusMinutes(i)));
		}
		List<LedgerEntry> user2Entries = ledger.getUserEntries(record1, user2);
		Assert.assertEquals("Add - multiple entries same record, different time", 
				count, user2Entries.size());
	}
	
	
	@Test
	public void addUser() throws Exception {
		
		int count = 3;
		OffsetDateTime now = OffsetDateTime.now();
		for (int i = 0; i < count; i++) {
			ledger.add(record1, user1, new Scorable(category1, 3, now.plusNanos(i)));
			ledger.add(record1, user2, new Scorable(category2, 4, now.plusNanos(i)));
			ledger.add(record1, user3, new Scorable(category3, 5, now.plusNanos(i)));
		}
		
		validateUserEntries(count, category1, ledger.getUserEntries(record1, user1));
		validateUserEntries(count, category2, ledger.getUserEntries(record1, user2));
		validateUserEntries(count, category3, ledger.getUserEntries(record1, user3));
	}

	
	@Test
	public void addRecord() throws Exception {
		
		int count = 3;
		OffsetDateTime now = OffsetDateTime.now();
		for (int i = 0; i < count; i++) {
			ledger.add(record1, user1, new Scorable(category1, 3, now.plusNanos(i)));
			ledger.add(record1, user2, new Scorable(category2, 4, now.plusNanos(i)));
			ledger.add(record1, user3, new Scorable(category3, 5, now.plusNanos(i)));
			
			ledger.add(record2, user1, new Scorable(category1, 3, now.plusNanos(i)));
			ledger.add(record2, user2, new Scorable(category2, 4, now.plusNanos(i)));
			ledger.add(record2, user3, new Scorable(category3, 5, now.plusNanos(i)));
		}
		
		validateUserEntries(count, category1, ledger.getUserEntries(record2, user1));
		validateUserEntries(count, category2, ledger.getUserEntries(record2, user2));
		validateUserEntries(count, category3, ledger.getUserEntries(record2, user3));
		
		Map<String, List<LedgerEntry>> recordEntries1 = ledger.getRecordEntries(record1);
		Map<String, List<LedgerEntry>> recordEntries2 = ledger.getRecordEntries(record2);
		
		Assert.assertEquals("Record1 user count", 3, recordEntries1.keySet().size());
		Assert.assertEquals("Record2 user count", 3, recordEntries2.keySet().size());
	}
}
