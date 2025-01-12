package com.ani.taku_backend.marketprice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Duration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        GenericJackson2JsonRedisSerializer customSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        // Key: String, Value: Jackson 직렬화
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(customSerializer)
                );

        return RedisCacheManager.builder(connectionFactory)
                .withCacheConfiguration("marketPrice",  // marketPrice 캐시: 10분
                        defaultConfig.entryTtl(Duration.ofMinutes(10)))
                .withCacheConfiguration("priceGraph",   // priceGraph 캐시: 1시간
                        defaultConfig.entryTtl(Duration.ofHours(1)))
                .withCacheConfiguration("weeklyStats",  // weeklyStats 캐시: 5분
                        defaultConfig.entryTtl(Duration.ofMinutes(5)))
                .withCacheConfiguration("similarProducts", // similarProducts 캐시: 30분
                        defaultConfig.entryTtl(Duration.ofMinutes(30)))
                .build();
    }
}