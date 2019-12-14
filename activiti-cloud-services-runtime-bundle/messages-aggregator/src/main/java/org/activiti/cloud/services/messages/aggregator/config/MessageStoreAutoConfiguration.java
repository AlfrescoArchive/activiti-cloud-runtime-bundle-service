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


import java.sql.SQLException;
import java.util.Arrays;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
//import org.springframework.boot.data.geode.autoconfigure.ClientCacheAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.integration.hazelcast.lock.HazelcastLockRegistry;
import org.springframework.integration.hazelcast.metadata.HazelcastMetadataStore;
import org.springframework.integration.hazelcast.store.HazelcastMessageStore;
import org.springframework.integration.jdbc.lock.DefaultLockRepository;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.integration.jdbc.lock.LockRepository;
import org.springframework.integration.jdbc.metadata.JdbcMetadataStore;
import org.springframework.integration.jdbc.store.JdbcMessageStore;
import org.springframework.integration.metadata.ConcurrentMetadataStore;
import org.springframework.integration.metadata.SimpleMetadataStore;
import org.springframework.integration.mongodb.metadata.MongoDbMetadataStore;
import org.springframework.integration.mongodb.store.ConfigurableMongoDbMessageStore;
import org.springframework.integration.mongodb.support.BinaryToMessageConverter;
import org.springframework.integration.mongodb.support.MessageToBinaryConverter;
import org.springframework.integration.redis.metadata.RedisMetadataStore;
import org.springframework.integration.redis.store.RedisMessageStore;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.integration.store.MessageGroupStore;
import org.springframework.integration.store.SimpleMessageStore;
import org.springframework.integration.support.locks.DefaultLockRegistry;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.transaction.HazelcastTransactionManager;


/**
 * A helper class containing configuration classes for particular technologies
 * to expose an appropriate {@link org.springframework.integration.store.MessageStore} bean
 * via matched configuration properties.
 *
 */
@Configuration
public class MessageStoreAutoConfiguration {

    @ConditionalOnProperty(prefix = MessageAggregatorProperties.PREFIX,
                           name = "message-store-type",
                           havingValue = MessageAggregatorProperties.MessageStoreType.SIMPLE)
    static class Simple {

        @Bean
        @ConditionalOnMissingBean
        public DataSource dataSource() throws SQLException {
            EmbeddedDatabaseBuilder databaseBuilder = new EmbeddedDatabaseBuilder();

            return databaseBuilder.generateUniqueName(true)
                                  .setType(EmbeddedDatabaseType.H2)
                                  .build();
        }

        @Bean
        @ConditionalOnMissingBean
        public PlatformTransactionManager transactionManager(DataSource dataSource) throws SQLException {
          return new DataSourceTransactionManager(dataSource);
        }
        
        @Bean
        @ConditionalOnMissingBean
        public MessageGroupStore messageStore() {
            return new SimpleMessageStore();
        }

        @Bean
        @ConditionalOnMissingBean
        public ConcurrentMetadataStore metadataStore() {
            return new SimpleMetadataStore();
        }
        
        @Bean
        @ConditionalOnMissingBean
        public LockRegistry lockRegistry() {
            return new DefaultLockRegistry();
        }
    }    
    

    @ConditionalOnClass(HazelcastInstance.class)
    @ConditionalOnProperty(prefix = MessageAggregatorProperties.PREFIX,
                           name = "message-store-type",
                           havingValue = MessageAggregatorProperties.MessageStoreType.HAZELCAST)
    static class Hazel {

        @Bean
        @ConditionalOnMissingBean
        public Config hazelcastConfig() {
            Config config = new Config();
            
            config.getCPSubsystemConfig()
                  .setCPMemberCount(3);
            
            return config;
        }
        
        @Bean
        @ConditionalOnMissingBean
        public PlatformTransactionManager txManager(HazelcastInstance hazelcastInstance) {
            return new HazelcastTransactionManager(hazelcastInstance);
        }
        
        
        @Bean
        @ConditionalOnMissingBean
        public MessageGroupStore messageStore(HazelcastInstance hazelcastInstance) {
            HazelcastMessageStore messageStore = new HazelcastMessageStore(hazelcastInstance);

            messageStore.setLazyLoadMessageGroups(false);
            
            return messageStore;
        }

        @Bean
        @ConditionalOnMissingBean
        public ConcurrentMetadataStore metadataStore(HazelcastInstance hazelcastInstance) {
            return new HazelcastMetadataStore(hazelcastInstance);
        }

        @Bean
        public HazelcastInstance hazelcastInstance(Config hazelcastConfig) {
            return Hazelcast.newHazelcastInstance(hazelcastConfig);
        }

        @Bean
        public HazelcastInstance hazelcastInstance2(Config hazelcastConfig) {
            return Hazelcast.newHazelcastInstance(hazelcastConfig);
        }

        @Bean
        public HazelcastInstance hazelcastInstance3(Config hazelcastConfig) {
            return Hazelcast.newHazelcastInstance(hazelcastConfig);
        }
        
        @Bean
        @ConditionalOnMissingBean
        public LockRegistry lockRegistry(HazelcastInstance hazelcastInstance) {
            return new HazelcastLockRegistry(hazelcastInstance);
        }    
    }    
    
    
    @ConditionalOnClass(ConfigurableMongoDbMessageStore.class)
    @ConditionalOnProperty(prefix = MessageAggregatorProperties.PREFIX,
                           name = "message-store-type",
                           havingValue = MessageAggregatorProperties.MessageStoreType.MONGODB)
    @Import({MongoAutoConfiguration.class,
             MongoDataAutoConfiguration.class})
    static class Mongo {

        @Bean
        public MessageGroupStore messageStore(MongoTemplate mongoTemplate, MessageAggregatorProperties properties) {
            ConfigurableMongoDbMessageStore messageStore;
            
            if (StringUtils.hasText(properties.getMessageStoreEntity())) {
                messageStore = new ConfigurableMongoDbMessageStore(mongoTemplate, properties.getMessageStoreEntity());
            }
            else {
                messageStore = new ConfigurableMongoDbMessageStore(mongoTemplate);
            }
            
            messageStore.setLazyLoadMessageGroups(false);
            
            return messageStore;
        }

        @Bean
        @Primary
        public MongoCustomConversions mongoDbCustomConversions() {
            return new MongoCustomConversions(Arrays.asList(
                    new MessageToBinaryConverter(), new BinaryToMessageConverter()));
        }
        
        @Bean
        public MongoTransactionManager transactionManager(MongoDbFactory mongoDbFactory) {
            return new MongoTransactionManager(mongoDbFactory);
        }        
        
        @Bean
        @ConditionalOnMissingBean
        public ConcurrentMetadataStore metadataStore(MongoTemplate mongoTemplate) {
            return new MongoDbMetadataStore(mongoTemplate);
        }
                
        @Bean
        @ConditionalOnMissingBean
        public LockRegistry lockRegistry() {
            return new DefaultLockRegistry();
        }

    }

    @ConditionalOnClass(RedisMessageStore.class)
    @ConditionalOnProperty(prefix = MessageAggregatorProperties.PREFIX,
                           name = "message-store-type",
                           havingValue = MessageAggregatorProperties.MessageStoreType.REDIS)
    @Import(RedisAutoConfiguration.class)
    static class Redis {

        @Autowired
        public void configure(RedisTemplate<Object, Object> redisTemplate) {
            redisTemplate.setEnableTransactionSupport(true);
        }        
        
        @Bean
        public MessageGroupStore messageStore(RedisTemplate<?, ?> redisTemplate) {
            RedisMessageStore messageStore = new RedisMessageStore(redisTemplate.getConnectionFactory());
            messageStore.setLazyLoadMessageGroups(false);
            
            return messageStore;
        }
        
        @Bean
        @ConditionalOnMissingBean
        public ConcurrentMetadataStore metadataStore(RedisConnectionFactory connectionFactory) {
            return new RedisMetadataStore(connectionFactory);
        }
        
        @Bean
        @ConditionalOnMissingBean
        public LockRegistry lockRegistry(RedisConnectionFactory connectionFactory) {
            return new RedisLockRegistry(connectionFactory, "RedisLockRegistry");
        }
        
        //Transaction datasource
        @Bean
        @ConditionalOnMissingBean
        public DataSource dataSource() throws SQLException {
            EmbeddedDatabaseBuilder databaseBuilder = new EmbeddedDatabaseBuilder();

            return databaseBuilder.generateUniqueName(true)
                                  .setType(EmbeddedDatabaseType.H2)
                                  .build();
        }

        @Bean
        @ConditionalOnMissingBean
        public PlatformTransactionManager transactionManager(DataSource dataSource) throws SQLException {
          return new DataSourceTransactionManager(dataSource);
        }


    }

//    @ConditionalOnClass(GemfireMessageStore.class)
//    @ConditionalOnProperty(prefix = AggregatorProperties.PREFIX,
//            name = "message-store-type",
//            havingValue = AggregatorProperties.MessageStoreType.GEMFIRE)
//    @Import(ClientCacheAutoConfiguration.class)
//    static class Gemfire {
//
//        @Bean
//        @ConditionalOnMissingBean
//        public ClientRegionFactoryBean<?, ?> gemfireRegion(GemFireCache cache, AggregatorProperties properties) {
//            ClientRegionFactoryBean<?, ?> clientRegionFactoryBean = new ClientRegionFactoryBean<>();
//            clientRegionFactoryBean.setCache(cache);
//            clientRegionFactoryBean.setName(properties.getMessageStoreEntity());
//            return clientRegionFactoryBean;
//        }
//
//        @Bean
//        public MessageGroupStore messageStore(Region<Object, Object> region) {
//            return new GemfireMessageStore(region);
//        }
//
//    }

    @ConditionalOnClass(JdbcMessageStore.class)
    @ConditionalOnProperty(prefix = MessageAggregatorProperties.PREFIX,
                           name = "message-store-type",
                           havingValue = MessageAggregatorProperties.MessageStoreType.JDBC)
    @Import({DataSourceAutoConfiguration.class,
             DataSourceTransactionManagerAutoConfiguration.class})
    static class Jdbc {

        @Bean
        public MessageGroupStore messageStore(JdbcTemplate jdbcTemplate, MessageAggregatorProperties properties) {
            JdbcMessageStore messageStore = new JdbcMessageStore(jdbcTemplate);
            messageStore.setLazyLoadMessageGroups(false);

            if (StringUtils.hasText(properties.getMessageStoreEntity())) {
                messageStore.setTablePrefix(properties.getMessageStoreEntity());
            }
            return messageStore;
        }
        
        @Bean
        @ConditionalOnMissingBean
        public ConcurrentMetadataStore metadataStore(JdbcTemplate jdbcTemplate) {
            return new JdbcMetadataStore(jdbcTemplate);
        }
        
        @Bean
        @ConditionalOnMissingBean
        public LockRepository lockRepository(DataSource dataSource) {
            return new DefaultLockRepository(dataSource);
        }
        
        @Bean
        @ConditionalOnMissingBean
        public LockRegistry lockRegistry(LockRepository lockRepository) {
            return new JdbcLockRegistry(lockRepository);
        }        

    }

}