package com.ani.taku_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

@Configuration
public class MongoConfig {

    @Bean
    public MongoMappingContext mongoMappingContext() {
        MongoMappingContext mappingContext = new MongoMappingContext();
        mappingContext.setFieldNamingStrategy(field -> {
            String fieldName = field.getName();
            // Java에서는 Camel Case, Mongo에서는 snake case
            return fieldName.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
        });
        return mappingContext;
    }
}