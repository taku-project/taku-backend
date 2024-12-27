package com.ani.taku_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableJpaAuditing
@EnableMongoRepositories
public class TakuProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(TakuProjectApplication.class, args);
        System.out.println("Hello World!");
    }

}
