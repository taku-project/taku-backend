package com.ani.taku_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;

//@Configuration
//public class MongoConfig {
//
//    @Bean
//    public MongoMappingContext mongoMappingContext() {
//        MongoMappingContext mappingContext = new MongoMappingContext();
//        mappingContext.setFieldNamingStrategy(field -> {
//            String fieldName = field.getName();
//            // Java에서는 Camel Case, Mongo에서는 snake case
//            return fieldName.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
//        });
//        return mappingContext;
//    }
//
//    @Bean
//    public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDbFactory, MappingMongoConverter converter) {
//        return new MongoTemplate(mongoDbFactory, converter);
//    }
//}