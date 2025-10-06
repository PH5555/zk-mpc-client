package com.zkrypto.zkmpc.common.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
@Profile("!prod")
public class MongoInitializer {
    @Bean
    public ApplicationRunner init(MongoTemplate mongoTemplate) {
        return args -> {
            var collectionNames = mongoTemplate.getCollectionNames();
            collectionNames.forEach(mongoTemplate::dropCollection);
        };
    }
}
