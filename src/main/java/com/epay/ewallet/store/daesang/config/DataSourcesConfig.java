package com.epay.ewallet.store.daesang.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

@Configuration
public class DataSourcesConfig {
    @Bean
    @Primary
    @ConfigurationProperties(prefix="spring.datasource.one")
    DataSource dsOne(){
        return DataSourceBuilder.create().build();
    }

    @Bean
    @ConfigurationProperties(prefix="spring.datasource.two")
    DataSource dsTwo(){
        return DataSourceBuilder.create().build();
    }
    
	/*
	 * @Bean
	 * 
	 * @ConfigurationProperties("spring.datasource.three") DataSource dsThree(){
	 * return DataSourceBuilder.create().build(); }
	 * 
	 * @Bean
	 * 
	 * @ConfigurationProperties("spring.datasource.four") DataSource dsFour(){
	 * return DataSourceBuilder.create().build(); }
	 */
    
    @Bean
    @Autowired
    @Primary
    DataSourceTransactionManager db1Tm(@Qualifier("dsOne") DataSource dataSource) {
        DataSourceTransactionManager dstm = new DataSourceTransactionManager(dataSource);
        return dstm;
    }

    @Bean
    @Autowired
    DataSourceTransactionManager db2Tm(@Qualifier("dsTwo") DataSource dataSource) {
        DataSourceTransactionManager dstm = new DataSourceTransactionManager(dataSource);
        return dstm;
    }
}
