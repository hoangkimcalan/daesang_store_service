package com.epay.ewallet.store.daesang.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@MapperScan(value = "com.epay.ewallet.store.daesang.mapperOne",sqlSessionFactoryRef = "sqlSessionFactorybean1")
public class MybatisConfigOne {
    @Autowired
    @Qualifier("dsOne") DataSource dsOne;
    @Bean
    SqlSessionFactory sqlSessionFactorybean1() throws Exception{
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dsOne);
        SqlSessionFactory sqlFactoryBean = factoryBean.getObject();
        sqlFactoryBean.getConfiguration().setJdbcTypeForNull(JdbcType.NULL);
        
        return sqlFactoryBean;
    }
    @Bean
    SqlSessionTemplate sqlSessionTemplate1() throws Exception{
        return new SqlSessionTemplate(sqlSessionFactorybean1());
    }
    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dsOne);
    }
}
