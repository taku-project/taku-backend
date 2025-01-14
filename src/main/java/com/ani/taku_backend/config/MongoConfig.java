package com.ani.taku_backend.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.*;

import com.ani.taku_backend.config.converter.InteractionTypeConverter;
import com.ani.taku_backend.config.converter.StringToInteractionTypeConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

@Configuration
@EnableMongoAuditing
public class MongoConfig {

    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDbFactory, MappingMongoConverter converter) {
        return new MongoTemplate(mongoDbFactory, converter);
    }

    /**
     * InteractionType 엔티티를 String으로 변환하는 컨버터
     */
    @Bean
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(Arrays.asList(
            new InteractionTypeConverter(),
            new StringToInteractionTypeConverter()
        ));
    }


}