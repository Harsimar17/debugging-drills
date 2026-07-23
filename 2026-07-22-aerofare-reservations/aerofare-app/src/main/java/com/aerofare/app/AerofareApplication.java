package com.aerofare.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.aerofare")
@EnableJpaRepositories(basePackages = "com.aerofare.repository")
@EntityScan(basePackages = "com.aerofare.domain")
@EnableScheduling
public class AerofareApplication {

    public static void main(String[] args) {
        SpringApplication.run(AerofareApplication.class, args);
    }
}
