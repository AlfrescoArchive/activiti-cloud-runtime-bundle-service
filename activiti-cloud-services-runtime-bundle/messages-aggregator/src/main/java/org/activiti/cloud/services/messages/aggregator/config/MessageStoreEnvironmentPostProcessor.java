/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.cloud.services.messages.aggregator.config;


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