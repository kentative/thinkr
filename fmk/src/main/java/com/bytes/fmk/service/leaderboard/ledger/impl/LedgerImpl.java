package com.bytes.fmk.service.leaderboard.ledger.impl;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bytes.fmk.service.Thinkr;
import com.bytes.fmk.service.leaderboard.ledger.Ledger;
import com.bytes.fmk.service.leaderboard.ledger.LedgerEntry;
import com.bytes.fmk.service.leaderboard.ledger.Recordable;
import com.bytes.fmk.service.persistence.PersistenceMode;
import com.bytes.fmk.service.persistence.Serializer;

/**
 * Hierarchical implementation
 * recordId              (1):    10 bytes 
 *  - userId            (10):   100 bytes
 *  	- recorableId    (1):    10 bytes
 *  		- time1   (1000): 10000 bytes
 *  		- time2
 *  		- time3
 *  total size = 10+100+10 + 1000 = 10,120 bytes
 * 
 * Flat implementation: 
 *  (recordId, userId, recorableId, times) x 1000
 *  (10 + 10 + 10 + 10) x 1000 
 *  = 40 x 100
 *  = 40,000 bytes (+ ~400%)
 * 
 * @author Kent
 */
public class LedgerImpl implements Ledger {

	
	public static final String RESOURCE_ID = "LeaderboardLedgerResourceID";

	private static Logger logger = LoggerFactory.getLogger(LedgerImpl.class);
	
	/**
	 * The default record id
	 */
	private transient String ledgerRecordId = "DefaultRecords";

	
	/**
	 * The ledger id
	 */
	private String id;
	
	/**
	 * The entries in this ledger
	 * <pre>
	 * Contents
	 * key = leaderboardId
	 *  - key = userId
	 *    - key = recordableId
	 *      - value = timestamp
	 * </pre>
	 */
	private Map<String, Map<String, Map<Integer, List<OffsetDateTime>>>> entries;
	
	/**
	 * The recordable lookup map
	 * key = recordableId
	 * value = description of the recordable
	 */
	private List<RecordableInfo> recordables;
	
	private transient Serializer serializer;
	
	/**
	 * Default constructor
	 */
	public LedgerImpl() {
		this.id = UUID.randomUUID().toString();
		this.entries = new ConcurrentHashMap<>();
		this.recordables = new ArrayList<>();
		this.serializer = new LedgerSerializer();
	}
	

	/**
	 * {@inheritDoc}
	 */
	public boolean add(String userId, Recordable recordable) {
		return add(ledgerRecordId, userId, recordable);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public boolean add(String recordId, String userId, Recordable recordable) {

		if (recordable == null) {
			logger.warn("Missing recordable. Entry will be skipped");
			return false;
		}
		
		if (!entries.containsKey(recordId)) {
			Map<String, Map<Integer, List<OffsetDateTime>>> userEntries = new ConcurrentHashMap<>();
			entries.put(recordId, userEntries);
		}
		return addUserRecord(entries.get(recordId), userId, recordable);
	}
	

	/**
	 * 
	 * @param userEntries - entries containing the hierarchy of userId, recordId, List<OffsetDateTime>
	 * @param userId - the user id
	 * @param recordable - the data to be recorded
	 * @return true if successfully added, false otherwise
	 */
	private boolean addUserRecord(
			Map<String, Map<Integer, List<OffsetDateTime>>> userEntries, 
			String userId, 
			Recordable recordable) {
		
		if (!userEntries.containsKey(userId)) {
			Map<Integer, List<OffsetDateTime>> entry = new ConcurrentHashMap<>();
			userEntries.put(userId, entry);
		}
		return addTimedEntry(userEntries.get(userId), recordable);
	}
	
	
	/**
	 * @param recordEntries - entries containing the hierarchy of recordId and the list of timestamp
	 * @param recordable - the data to be recorded
	 * @return true if successfully added, false otherwise
	 */
	private boolean addTimedEntry(
			Map<Integer, List<OffsetDateTime>> recordEntries, 
			Recordable recordable) {
		
		boolean isUniqueEntry = false;
		
		int recordId = getRecordableId(recordable);
		if (recordId < 0) {
			logger.error(String.format(
					"Unable to get recordable id. Entry will be skipped: %1$s - %2$d " ,
					recordable.getCategoryName(), recordable.getPoints()));
			return false;
		}
		
		if (!recordEntries.containsKey(recordId)) {
			List<OffsetDateTime> timeStamps = new ArrayList<>();
			recordEntries.put(recordId, timeStamps);
			isUniqueEntry = true;
		}
		
		List<OffsetDateTime> timeStamps = recordEntries.get(recordId);
		OffsetDateTime time = recordable.getTime();
		if (time == null) {
			time = OffsetDateTime.now();
			isUniqueEntry = true;
			logger.warn("Recordable has no time, defaulting to now, this can potentially result in duplicate record.");
		}
		
		isUniqueEntry |= !timeStamps.contains(time); 
		if (isUniqueEntry) {
			timeStamps.add(time);
			recordEntries.put(recordId, timeStamps);
			logger.trace("Added recordable time " + time.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
		} else {
			logger.trace("Duplicate entries detected, likely from a leaderboard set.");
		}
		return true;
	}
	
	
	/**
	 * Get the id of the recordableInfo 
	 * @param recordable
	 * @return
	 */
	private int getRecordableId(Recordable recordable) {
		
		if (recordable == null || 
				recordable.getCategoryName() == null) {
			throw new IllegalArgumentException("Invalid recordable");
		}
		
		RecordableInfo r = new RecordableInfo(recordable.getCategoryName(), recordable.getPoints());
		int index = recordables.indexOf(r);
		if (index < 0) {
			if (recordables.add(r)) {
				return recordables.size()-1;
			} 
			return -1;
		}
		return index;
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getId() {
		return id;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, List<LedgerEntry>> getRecordEntries() {
		return getRecordEntries(ledgerRecordId);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, List<LedgerEntry>> getRecordEntries(String recordId) {
		
		// UserId, LedgerEntry
		Map<String, Map<Integer, List<OffsetDateTime>>> userEntries = entries.get(recordId);
		
		Map<String, List<LedgerEntry>> recordEntries = new ConcurrentHashMap<>();
		for (String userId : userEntries.keySet()) {
			List<LedgerEntry> entries = createLedgerEntry(userEntries.get(userId));
			recordEntries.put(userId, entries);
		}
		return recordEntries;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<LedgerEntry> getUserEntries(String recordId, String userId) {
		return createLedgerEntry(entries.get(recordId).get(userId));
	}


	/**
	 * Creates the list {@code LedgerEntry} from the 
	 * {@code RecordableInfo} and the list of timestamps.
	 * @param timedEntries - the map containing the categorized timestamps
	 * @return the list of {@code LedgerEntry}
	 */
	private List<LedgerEntry> createLedgerEntry(Map<Integer, List<OffsetDateTime>> timedEntries) {
		List<LedgerEntry> ledgerEntries = new ArrayList<>();
		timedEntries.keySet().forEach(id -> {
			timedEntries.get(id).forEach(time -> {
				RecordableInfo recordableInfo = recordables.get(id);
				ledgerEntries.add(new LedgerEntry(
						recordableInfo.name, recordableInfo.points, time));
			});
		});
		return ledgerEntries;
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean save(PersistenceMode mode, String resourceID) {
		return Thinkr.INSTANCE.getPersistenceService(mode)
				.saveData(resourceID, this);
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Ledger load(PersistenceMode mode, String resourceID) {
		
		try {
			LedgerImpl loaded = Thinkr.INSTANCE.getPersistenceService(mode)
					.loadData(resourceID, getSerializer());
			
			this.id = loaded.id;
			this.entries = loaded.entries;
			this.recordables = loaded.recordables;
			
		} catch (Exception e) {
			logger.error("Unable to load data: " + e.getMessage());
		}
		return this;
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Serializer getSerializer() {
		return serializer;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getSize() {
		long count = 0;
		for (String recordId : entries.keySet()) {
			Map<String, Map<Integer, List<OffsetDateTime>>> userEntries = entries.get(recordId);
			for (String userId : userEntries.keySet()) {
				Map<Integer, List<OffsetDateTime>> recordables = userEntries.get(userId);
				for (int recordableId : recordables.keySet()) {
					count += recordables.get(recordableId).size();
				}
			}
		}
		return count;
	}
}
