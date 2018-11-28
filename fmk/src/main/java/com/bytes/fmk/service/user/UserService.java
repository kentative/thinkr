package com.bytes.fmk.service.user;

import java.util.Collection;

import com.bytes.fmk.data.model.User;
import com.bytes.fmk.service.ThinkrService;
import com.bytes.fmk.service.persistence.Persistable;

public interface UserService<T extends User> extends ThinkrService, Persistable<UserService<T>>{


	/**
	 * Request to register the user
	 * @param userId - the userId
	 * @param user - the user
	 * @return true if successfully added, false if user is already registered
	 */
	public boolean registerUser(String userId, T user);
	
	
	/**
	 * Indicates if the user is registered
	 * @param userId - the user id
	 * @return true if the user is registered
	 */
	public boolean isRegistered(String userId);
	
	
	/**
	 * Request to get the registered user specified by the user Id
	 * @param userId - the user id
	 * @return the user data
	 */
	public T getUser(String userId);
	
	
	/**
	 * Request to unregister the user 
	 * @param userId
	 * @return the removed user instance
	 */
	public T unregisterUser(String userId);


	/**
	 * Get all registered users
	 * @return
	 */
	public Collection<T> getUsers();

	
}
