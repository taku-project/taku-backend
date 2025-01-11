package com.ani.taku_backend.marketprice.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.test.context.TestConfiguration;
import redis.embedded.RedisServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TestConfiguration
public class TestRedisConfiguration {
    private static final Logger log = LoggerFactory.getLogger(TestRedisConfiguration.class);

    private RedisServer redisServer;

    public TestRedisConfiguration() {
        this.redisServer = new RedisServer(6379);
    }

    @PostConstruct
    public void postConstruct() {
        try {
            redisServer.start();
            log.info("Embedded Redis started");
        } catch (Exception e) {
            log.error("Failed to start embedded Redis", e);
        }
    }

    @PreDestroy
    public void preDestroy() {
        if (redisServer != null) {
            redisServer.stop();
            log.info("Embedded Redis stopped");
        }
    }
}