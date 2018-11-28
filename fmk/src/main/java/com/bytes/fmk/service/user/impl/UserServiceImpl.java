package com.bytes.fmk.service.user.impl;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bytes.fmk.data.model.User;
import com.bytes.fmk.service.Thinkr;
import com.bytes.fmk.service.persistence.PersistenceMode;
import com.bytes.fmk.service.user.UserService;

public class UserServiceImpl<T extends User> implements UserService<T> {

	private static Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
	
	private transient UserServiceSerializer<T> serializer;
	
	/**
	 * Map of registered users
	 * key = userId:String
	 * value = {@code User}
	 */
	private Map<String, T> registeredUsers;
	
	
	/**
	 * Default constructor
	 */
	public UserServiceImpl() {
		this.registeredUsers = new ConcurrentHashMap<>();
		this.serializer = new UserServiceSerializer<T>();
	}
	
	
	@Override
	public boolean registerUser(String userId, T user) {
		
		if(user == null || userId == null) {
			throw new IllegalArgumentException("User Id or user is not specified.");
		}
		
		if (registeredUsers.containsKey(userId)) {
			logger.warn("User is already registered: " + userId);
			return false;
		}
		
		registeredUsers.put(userId, user);
		return true;
	}

	
	@Override
	public T getUser(String userId) {
		return registeredUsers.get(userId);
	}

	
	@Override
	public T unregisterUser(String userId) {
		return registeredUsers.remove(userId);
	}


	@Override
	public boolean isRegistered(String userId) {
		return registeredUsers.containsKey(userId);
	}


	@Override
	public boolean save(PersistenceMode mode, String resourceID) {
		return Thinkr.INSTANCE.getPersistenceService(mode)
				.saveData(resourceID, this);
	}


	@Override
	public UserService<T> load(PersistenceMode mode, String resourceID) {
		
		try {
			UserServiceImpl<T> loadedInstance = 
					Thinkr.INSTANCE.getPersistenceService(mode)
						.loadData(resourceID, serializer);
			
			
			this.registeredUsers = loadedInstance.registeredUsers;
		} catch (Exception e) {
			logger.error("Unable to load user data: " + e.getMessage());
		}
		return this;
	}


	@Override
	public  Collection<T> getUsers() {
		return registeredUsers.values();
	}


	public UserServiceSerializer<T> getSerializer() {
		return serializer;
	}

}
