package com.epay.ewallet.store.daesang.redis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.epay.ewallet.store.daesang.config.AppConfig;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Component
public class RedisService {

	private static final Logger log = LogManager.getLogger(RedisService.class);
	
	@Autowired
	JedisPool pool;
	
	@Autowired
	AppConfig appConfig;
	
	public String get(String key) throws Exception {
		try (Jedis redis = pool.getResource();) {
			redis.select(appConfig.REDIS_DB);
			String value = redis.get(key);
			return value;
		}
	}
	
}
