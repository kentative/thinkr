package com.bytes.fmk.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bytes.fmk.data.model.User;
import com.bytes.fmk.service.leaderboard.LeaderboardService;
import com.bytes.fmk.service.leaderboard.impl.LeaderboardServiceImpl;
import com.bytes.fmk.service.persistence.PersistenceMode;
import com.bytes.fmk.service.persistence.PersistenceService;
import com.bytes.fmk.service.persistence.impl.RedisImpl;
import com.bytes.fmk.service.user.UserService;
import com.bytes.fmk.service.user.impl.UserServiceImpl;

public enum Thinkr {
	
	INSTANCE;
	
	public enum Service {
		
		PersistenceAzure,
		
		PersistenceLocal,
		
		Leaderboard, 
		
		User
	}
	
	private static Logger logger = LoggerFactory.getLogger(Thinkr.class);
	
	private Map<Service, ThinkrService> services;
	
	private Thinkr() {
		services = new HashMap<>();
	}
	
	
	/**
	 * Retrieve the {@code LeaderboardService} service.
	 * @return the requested service
	 */
	public LeaderboardService getLeaderboardService() {
	
		if (services.containsKey(Service.Leaderboard)) {
			return (LeaderboardService) services.get(Service.Leaderboard);
		} 
		return (LeaderboardService) getService(Service.Leaderboard);
	}
	
	
	/**
	 * Retrieve the {@code PersistenceService} service.
	 * @return the requested service
	 */
	public PersistenceService getPersistenceService() {
	
		if (services.containsKey(Service.PersistenceAzure)) {
			return (PersistenceService) services.get(Service.PersistenceAzure);
		} 
		return (PersistenceService) getService(Service.PersistenceAzure);
	}
	
	
	/**
	 * Retrieve the {@code PersistenceService} service.
	 * @return the requested service
	 */
	public PersistenceService getPersistenceService(PersistenceMode mode) {
	
		Service service;
		switch (mode) {
		case RedisAzure:
			service = Service.PersistenceAzure;
			break;
			
		case RedisLocal:
			service = Service.PersistenceLocal;
			break;
			
		default:
			service = Service.PersistenceAzure;
			break;
		
		} 
		
		return (services.containsKey(service)) 
				?(PersistenceService) services.get(service) 
						:(PersistenceService) getService(service);
	}
	
	
	/**
	 * Retrieve the {@code UserService}
	 * @return the requested service
	 */
	@SuppressWarnings("unchecked")
	public <T extends User> UserService<T> getUserService() {
		
		if (services.containsKey(Service.User)) {
			return (UserService<T>) services.get(Service.User);
		} 
		services.put(Service.User, new UserServiceImpl<T>());
		return (UserService<T>) services.get(Service.User);
	}
	
	
	
	/**
	 * Request to retrieve the specified service
	 * @param service - the service type
	 * @return the requested service
	 */
	private ThinkrService getService(Service service) {
		
		switch (service) {
		case Leaderboard:
			services.put(service, new LeaderboardServiceImpl());
			break;
			
		case PersistenceAzure:
			services.put(service, RedisImpl.AzureThinkr);
			break;
			
		case PersistenceLocal:
			services.put(service, RedisImpl.Local);
			break;
			
		default:
			logger.error("Service not supported: " + service);
			break;
		}
		return services.get(service);
	}

}
