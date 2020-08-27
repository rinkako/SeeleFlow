/*
 * Author : Rinka
 * Date   : 2020/2/12
 */
package org.yurily.seele.server.steady.bean;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Class : DataSourceConfig
 * Usage :
 */
@Configuration
public class DataSourceConfig {

    @Primary
    @Bean(name = "seeleDataSource")
    @Qualifier("seeleDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.seele")
    public DataSource seeleDataSource() {
        return DataSourceBuilder.create().build();
    }
}
