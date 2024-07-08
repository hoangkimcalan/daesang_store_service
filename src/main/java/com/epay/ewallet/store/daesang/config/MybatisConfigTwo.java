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

@Configuration
@MapperScan(value = "com.epay.ewallet.mapper.mapperDataTwo",sqlSessionFactoryRef = "sqlSessionFactorybean2")
public class MybatisConfigTwo {
    @Autowired
    @Qualifier("dsTwo") DataSource dsTwo;
    @Bean
    SqlSessionFactory sqlSessionFactorybean2() throws Exception{
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dsTwo);
        SqlSessionFactory sqlFactoryBean = factoryBean.getObject();
        sqlFactoryBean.getConfiguration().setJdbcTypeForNull(JdbcType.NULL);
        
        return sqlFactoryBean;
    }
    @Bean
    SqlSessionTemplate sqlSessionTemplate2() throws Exception{
        return new SqlSessionTemplate(sqlSessionFactorybean2());
    }
}
