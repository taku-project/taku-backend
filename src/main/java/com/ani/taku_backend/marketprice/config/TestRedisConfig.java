//package com.ani.taku_backend.marketprice.config;
//
//
//import jakarta.annotation.PostConstruct;
//import jakarta.annotation.PreDestroy;
//import org.springframework.boot.test.context.TestConfiguration;
//import org.springframework.data.redis.connection.RedisServer;
//import redis.embedded.*;
//
//@TestConfiguration
//public class TestRedisConfig {
//
//    private RedisServer redisServer;
//
//    public TestRedisConfig() {
//        this.redisServer = new RedisServer(6379);
//    }
//
//    @PostConstruct
//    public void postConstruct() {
//        redisServer.start();
//    }
//
//    @PreDestroy
//    public void preDestroy() {
//        redisServer.stop();
//    }
//}