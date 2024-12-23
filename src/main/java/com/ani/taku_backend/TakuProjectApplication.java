package com.ani.taku_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class TakuProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(TakuProjectApplication.class, args);
        System.out.println("Hello World!");
    }

}
