/*
 * Author : Rinka
 * Date   : 2020/2/12
 */
package org.rinka.seele.server.steady.bean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.util.Map;

/**
 * Class : JpaConfigSeele
 * Usage :
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "entityManagerFactorySeele",
        transactionManagerRef = "transactionManagerSeele",
        basePackages = {"org.rinka.seele.server.steady.seele.repository"})
public class JpaConfigSeele {

    @Autowired
    @Qualifier("seeleDataSource")
    private DataSource seeleDataSource;

    @Autowired
    private JpaProperties jpaProperties;

    @Autowired
    private HibernateProperties hibernateProperties;

    @Primary
    @Bean(name = "entityManagerSeele")
    public EntityManager entityManager(EntityManagerFactoryBuilder builder) {
        return entityManagerFactorySeele(builder).getObject().createEntityManager();
    }

    @Primary
    @Bean(name = "entityManagerFactorySeele")
    public LocalContainerEntityManagerFactoryBean entityManagerFactorySeele(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(seeleDataSource)
                .packages("org.rinka.seele.server.steady.seele.entity")
                .persistenceUnit("seelePersistenceUnit")
                .properties(getVendorProperties())
                .build();
    }

    private Map<String, Object> getVendorProperties() {
        return hibernateProperties.determineHibernateProperties(jpaProperties.getProperties(), new HibernateSettings());
    }

    @Primary
    @Bean(name = "transactionManagerSeele")
    public PlatformTransactionManager transactionManagerSeele(EntityManagerFactoryBuilder builder) {
        return new JpaTransactionManager(entityManagerFactorySeele(builder).getObject());
    }
}
