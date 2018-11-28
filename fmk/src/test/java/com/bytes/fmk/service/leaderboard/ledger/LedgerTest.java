package com.bytes.fmk.service.leaderboard.ledger;

import java.util.List;

import org.junit.Assert;

public class LedgerTest {

	
	
	
	/**
	 * Validate the content of the ledger entry.
	 * Does not validate time.
	 * @param count - the number of users
	 * @param categoryName - the score category name
	 * @param userEntries - the list of {@code LedgerEntry} 
	 */
	protected void validateUserEntries(int count, String categoryName, List<LedgerEntry> userEntries) {
		Assert.assertEquals("User entry count",  count, userEntries.size());
		Assert.assertEquals("category name", categoryName, userEntries.get(0).getCategoryName());
		Assert.assertEquals("category name", categoryName, userEntries.get(1).getCategoryName());
		Assert.assertEquals("category name", categoryName, userEntries.get(2).getCategoryName());
	}
}
