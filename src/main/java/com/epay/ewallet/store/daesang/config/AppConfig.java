package com.epay.ewallet.store.daesang.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Component;

@Component
@PropertySources ({
	@PropertySource("classpath:application.properties")
})
public class AppConfig {
	
	@Value("${REDIS_HOST}")
	public String REDIS_HOST;

	@Value("${REDIS_PORT}")
	public int REDIS_PORT;

	@Value("${REDIS_AUTH}")
	public int REDIS_AUTH;

	@Value("${REDIS_PASSWORD}")
	public String REDIS_PASSWORD;
	
	@Value("${REDIS_DB}")
	public int REDIS_DB;
	
	@Value("${redis.max-total}")
	public int max_total;
	
	@Value("${redis.min-idle}")
	public int min_idle;
	
	@Value("${redis.max_idle}")
	public int max_idle;
	
	@Value("${redis.max-wait}")
	public long max_wait;
	
	@Value("${redis.time-between-eviction-runs}")
	public long time_between_eviction_runs;
	
	@Value("${redis.min-evictable-idle-time}")
	public long min_evictable_idle_time;
}
