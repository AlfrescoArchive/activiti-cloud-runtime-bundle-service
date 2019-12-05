package org.activiti.cloud.services.message.connector.config;


import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.hazelcast.HazelcastAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;

//TODO import org.springframework.boot.data.geode.autoconfigure.ClientCacheAutoConfiguration;

import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;

/**
 * An {@link EnvironmentPostProcessor} to add {@code spring.autoconfigure.exclude} property
 * since we can't use {@code application.properties} from the library perspective.
 *
 */
public class MessageStoreEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        MutablePropertySources propertySources = environment.getPropertySources();
        Properties properties = new Properties();

        properties.setProperty("spring.autoconfigure.exclude",
                               Stream.of(DataSourceAutoConfiguration.class,
                                         DataSourceTransactionManagerAutoConfiguration.class,
                                         MongoAutoConfiguration.class,
                                         MongoDataAutoConfiguration.class,
                                         MongoRepositoriesAutoConfiguration.class,
                                         EmbeddedMongoAutoConfiguration.class,
                                         RedisAutoConfiguration.class,
                                         HazelcastAutoConfiguration.class,
//                                         ClientCacheAutoConfiguration.class, // TODO GemFire configuration 
                                         RedisRepositoriesAutoConfiguration.class)
                                     .map(Class::getName)
                                     .collect(Collectors.joining(",")));

        propertySources.addLast(
                new PropertiesPropertySource("aggregator.exclude.stores.auto-configuration", properties));
    }

}