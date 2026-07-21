package com.vantage.loyalty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VantageRewardsApplication {

    public static void main(String[] args) {
        SpringApplication.run(VantageRewardsApplication.class, args);
    }
}
