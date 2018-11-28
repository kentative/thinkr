package com.bytes.fmk.service.leaderboard.ledger;

import java.util.List;
import java.util.Map;

import com.bytes.fmk.service.persistence.Persistable;

/**
 *
 */
public interface Ledger extends Persistable<Ledger> {


	/**
	 * Request to log a recordable action
	 * @param recordId - the ledger record id
	 * @param userId - the userId
	 * @param recordable - the recordable to log
	 * @return true if successfully, false otherwise
	 */
	boolean add(String recordId, String userId, Recordable recordable);
	
	
	/**
	 * Request to log a recordable action with a default record id
	 * @param userId - the user id
	 * @param recordable - the recordable to log
	 * @return true if successfully, false otherwise
	 */
	boolean add(String userId, Recordable recordable);
	
	
	/**
	 * Get the list list of {@code LedgerEntry}s grouped by user id from a 
	 * specific record
	 * @param recordId - the record id
	 * @return - the list of ledger entries grouped by user id
	 */
	public Map<String, List<LedgerEntry>> getRecordEntries(String recordId);
	
	
	/**
	 * Get the list list of {@code LedgerEntry}s grouped by user id
	 * @return - the list of ledger entries grouped by user id
	 */
	public Map<String, List<LedgerEntry>> getRecordEntries();
	
	
	/**
	 * Get the entries corresponding to the user. Returns null if not found.
	 * @param recordId - the ledger record id
	 * @param userId - the userId
	 * @return the list of entries, null if not found.
	 */
	public List<LedgerEntry> getUserEntries(String recordId, String userId);
	
	
	/**
	 * Get the ledger id
	 * @return
	 */
	String getId();


	/**
	 * Get the total number of entries of all records and all users in this ledger.
	 * @return the total number of entries
	 */
	long getSize();


}
