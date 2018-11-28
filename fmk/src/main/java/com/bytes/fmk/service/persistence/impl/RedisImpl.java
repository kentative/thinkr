package com.bytes.fmk.service.persistence.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bytes.fmk.service.persistence.PersistenceService;
import com.bytes.fmk.service.persistence.Serializer;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;

public enum RedisImpl implements PersistenceService {

	/**
	 * Local connection to Thinkr Redis cache
	 * <ol>
	 * To run a local server with authentication
	 * <li>Install redis - Kenacity > Udacity > Redis
	 * <li>$>src/redis-server [configuration file] </li>
   	 * <li>configuration file = redist.conf > requiredpass thinkr </li>
	 */
	Local("localhost", 6379, 
			"thinkr", "LocalClient", false,
			10000, 10000),
	
	/**
	 * Azure connection to Thinkr Redis cache
	 * 
	 * Enable non-ssl to connect via redis-cli
	 * src/redis-cli -h thinkr.redis.cache.windows.net -p 6379 -a jly5eEv8ZddNlnjvRiikXD9gA2PIHUUzoBv8s3ILY/w=
	 */
	AzureThinkr("thinkr.redis.cache.windows.net", 6380, 
			"jly5eEv8ZddNlnjvRiikXD9gA2PIHUUzoBv8s3ILY/w=", "AzureClient", true,
			60000, 60000);
	
	private static Logger logger = LoggerFactory.getLogger(RedisImpl.class);
	private static Object staticLock = new Object();

	// Connection parameters
	private String clientId;
	private String host;
	private int port;
	private String password;
	private boolean useSSL;
	private int connectionTimeout;
	private int operationTimeout;
	
	private JedisPool pool;

	/**
	 * 
	 * @param host - Redis host name
	 * @param port - Redis port (usually 6379 for local, 6380 for ssl)
	 * @param password - Redis connection authorization password
	 * @param clientId - The client id
	 * @param connTimeout - the connection timeout
	 * @param operationTimeout - the operation timeout
	 */
	private RedisImpl(
			String host, int port, String password, String clientId, boolean useSSL,
			int connTimeout, int operationTimeout) {
		
		this.host = host;
		this.clientId = clientId;
		this.port = port;
		this.password = password;
		this.useSSL = useSSL;
		this.connectionTimeout = connTimeout;
		this.operationTimeout = operationTimeout;
		
	}

    /**
     * Get the Redis pool instance
     * @return the instance of the Redis pool
     */
    public JedisPool getPoolInstance() {
        if (pool == null) { 
        	// avoid synchronization lock if initialization has already happened
            synchronized(staticLock) {
                if (pool == null) { 
                    JedisPoolConfig poolConfig = getPoolConfig();
                    pool = new JedisPool(poolConfig, 
                    		host, port, connectionTimeout, operationTimeout, 
                    		password, 0, clientId, 
                    		useSSL, null, null, null);
                }
            }
        }
        return pool;
    }
    
	private JedisPoolConfig getPoolConfig() {
		JedisPoolConfig poolConfig = new JedisPoolConfig();

		// Each thread trying to access Redis needs its own Jedis instance from the pool.
		// Using too small a value here can lead to performance problems, too big and
		// you have wasted resources.
		int maxConnections = 100;
		poolConfig.setMaxTotal(maxConnections);
		poolConfig.setMaxIdle(maxConnections);

		// Using "false" here will make it easier to debug when your
		// maxTotal/minIdle/etc settings need adjusting.
		// Setting it to "true" will result better behavior when unexpected load hits in production
		poolConfig.setBlockWhenExhausted(true);

		// How long to wait before throwing when pool is exhausted
		poolConfig.setMaxWaitMillis(operationTimeout);

		// This controls the number of connections that should be maintained for bursts of load.
		// Increase this value when you see pool.getResource() taking a long time to
		// complete under burst scenarios
		poolConfig.setMinIdle(10);

		return poolConfig;
	}

	/**
	 * Get information on current pool usage.
	 * @return information on Redis pool usage.
	 */
	public String getPoolCurrentUsage() {
		JedisPool jedisPool = getPoolInstance();
		JedisPoolConfig poolConfig = getPoolConfig();

		int active = jedisPool.getNumActive();
		int idle = jedisPool.getNumIdle();
		int total = active + idle;
		String log = String.format(
				"JedisPool: Active=%d, Idle=%d, Waiters=%d, total=%d, maxTotal=%d, minIdle=%d, maxIdle=%d", active,
				idle, jedisPool.getNumWaiters(), total, poolConfig.getMaxTotal(), poolConfig.getMinIdle(),
				poolConfig.getMaxIdle());

		return log;
	}
	
	/**
	 * Request to persist the specified data object with a key
	 * @param resourceId - the key to retrieve the data
	 * @param data - the data to be persisted
	 * @return true if successful
	 */
	public <D> boolean saveData(String resourceId, D data) {
		
		validateCommitArguments(resourceId, data);
		
		try (Jedis jedis = getPoolInstance().getResource()) {
			jedis.set(resourceId, Serializer.toJson(data));
		} catch (Exception e) {
			logger.error("Unable to data for : " + resourceId, e);
			return false;
		}
		return true;
	}
	
	/**
	 * Request to load the data corresponding to the specified key
	 * @param key - the key corresponding to the data
	 * @param serializer - contains the the key to token type mapping for json deserialization
	 * @return the data, null if not found;
	 */
	public <D> D loadData(String resourceId, Serializer serializer) {
		
		validateLoadArguments(resourceId, serializer);
		
		D result = null;
		String responseData;
		try (Jedis jedis = getPoolInstance().getResource()) {
			responseData = jedis.get(resourceId);
			logger.trace("Response from Jedis: " + responseData);
		
			if (responseData == null) {
				logger.warn("No data found for resourceId: " + resourceId);
				return null;
			}
	
			// Convert the string value to the strongly-type object
			result = serializer.getData(responseData, resourceId);
		} catch (Exception e) {
			logger.error("Unable to load data for: " + resourceId + " type " + 
					serializer.getResourceIdMap().get(resourceId), e);
			
		}
		return result;
		
	}


	/**
	 * Request to persist the specified map.
	 * This implementation uses a pooled connection and a pipeline 
	 * for efficiency.
	 * @param resourceId - the resource id
	 * @param map - the map to be persisted. The key must be a string. 
	 */
	@Override
	public <D> boolean saveMap(String resourceId, Map<String, D> map) {

		validateCommitArguments(resourceId, map);
		
		try (Jedis jedis = getPoolInstance().getResource()) {
			Pipeline pipeline = jedis.pipelined();
			for (String key : map.keySet()) {
				D data = map.get(key);
				pipeline.hset(resourceId, key, Serializer.toJson(data));
			}
			pipeline.sync();
		} catch (Exception e) {
			logger.error("Unable to persist map for : " + resourceId, e);
			return false;
		}

		return true;
	}

	
	/**
	 * Request to load a map specified by the resource id.
	 * This implementation uses a pooled connection.
	 * @param resourceId - the resource id
	 * @param serializer - the serializer to convert the JSON data 
	 * from Redis into a strongly typed map
	 * @return the map corresponding to the specified resource id, 
	 * empty if it doesn't exists. Note that the map key must be a String.
	 */
	@Override
	public <D> Map<String, D> loadMap(String resourceId, Serializer serializer) {

		validateLoadArguments(resourceId, serializer);
		
		Map<String, D> result = new HashMap<>();
		Map<String, String> responseData;
		try (Jedis jedis = getPoolInstance().getResource()) {
			responseData = jedis.hgetAll(resourceId);
			logger.trace("Response from Jedis: " + responseData);
		}

		// Convert the string value to the strongly-type object
		try {
			for (String key : responseData.keySet()) {
				D data = serializer.getData(responseData.get(key), resourceId);
				result.put(key, data);
			}
		} catch (Exception e) {
			logger.error("Unable to load map for : " + resourceId, e);
		}
		return result;
	}
	
	/**
	 * Request to clear all data associated with the specified resource id
	 * @param resourceId - the resource id
	 * @return true if the one or more entries were deleted, false otherwise.
	 */
	public boolean clear(String resourceId) {
		Jedis jedis = getPoolInstance().getResource();
		return (jedis.del(resourceId) > 0);
	}
	
	
	/**
	 * Validate arguments for save operations.
	 * @param resourceId - the resourceId
	 * @param data - the data
	 */
	private <D> void validateCommitArguments(String resourceId, D data) {
		
		if (resourceId == null) {
			throw new IllegalArgumentException("ResourceId cannot be null");
		}
		
		if (data == null) {
			throw new IllegalArgumentException("Data to be persisted cannot be null");
		}
	}
	
	
	/**
	 * Validates the arguments for load operations
	 * @param resourceId - the resourceId
	 * @param serializer - the serializer 
	 */
	private void validateLoadArguments(String resourceId, Serializer serializer) {
		
		if (resourceId == null) {
			throw new IllegalArgumentException("ResourceId cannot be null");
		}
		
		if (serializer == null) {
			throw new IllegalArgumentException("Serializer cannot be null");
		}
	}
}
