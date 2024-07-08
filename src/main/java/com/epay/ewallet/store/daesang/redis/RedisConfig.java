package com.epay.ewallet.store.daesang.redis;

import java.time.Duration;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.epay.ewallet.store.daesang.config.AppConfig;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;

@Configuration
public class RedisConfig {
	
	@Autowired
	AppConfig appConfig;
	
	@Bean
	JedisPool createJedisPool() {
		GenericObjectPoolConfig<Jedis> poolConfig = buildPoolConfig();
		JedisPool pool = null;
		if (appConfig.REDIS_AUTH == 1) {
			pool = new JedisPool(poolConfig, appConfig.REDIS_HOST, appConfig.REDIS_PORT, Protocol.DEFAULT_TIMEOUT, appConfig.REDIS_PASSWORD);
		} else {
			pool = new JedisPool(poolConfig, appConfig.REDIS_HOST, appConfig.REDIS_PORT, Protocol.DEFAULT_TIMEOUT);
		}
		
		return pool;
	}
	
	private GenericObjectPoolConfig<Jedis> buildPoolConfig() {
		GenericObjectPoolConfig<Jedis> poolConfig = new GenericObjectPoolConfig<>();
	    poolConfig.setMaxIdle(appConfig.max_idle);
	    poolConfig.setMinIdle(appConfig.min_idle);
	    poolConfig.setMaxWait(Duration.ofMillis(appConfig.max_wait));
	    poolConfig.setMaxTotal(appConfig.max_total);
	    poolConfig.setTestOnBorrow(true);
	    poolConfig.setTestOnReturn(true);
	    poolConfig.setTimeBetweenEvictionRuns(Duration.ofMillis(appConfig.time_between_eviction_runs));
	    poolConfig.setMinEvictableIdleTime(Duration.ofMillis(appConfig.min_evictable_idle_time));
	    return poolConfig;
	}
	
}
