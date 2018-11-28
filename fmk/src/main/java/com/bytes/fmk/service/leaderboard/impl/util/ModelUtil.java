package com.bytes.fmk.service.leaderboard.impl.util;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bytes.fmk.service.leaderboard.Score;
import com.bytes.fmk.service.leaderboard.ledger.LedgerEntry;

@Deprecated
public class ModelUtil {

	private static Logger logger = LoggerFactory.getLogger(ModelUtil.class);
	
	/**
	 * Reconstruct the {@code Score} data.
	 * Sets the category name
	 * @param scoreMap
	 */
	public static void reconstructScoreName(Map<String, Score> scoreMap) {
		
		for (String categoryName : scoreMap.keySet()) {
			Score score = scoreMap.get(categoryName);
			score.setCategoryName(categoryName);
		}
		return;
		
	}

	public static void reconstructLedgerEntries(List<LedgerEntry> entries) {
		for(LedgerEntry entry : entries) {
			if (entry.getTime() == null) {
				logger.debug("Updating ledger entry time to now");
				entry.setTime(OffsetDateTime.now());
			}
		}
	}

}
