package com.routbuddy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.routbuddy")
@EntityScan(basePackages = "com.routbuddy")
@EnableJpaRepositories(basePackages = "com.routbuddy")
@EnableAsync
@EnableScheduling
public class RoutBuddyApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoutBuddyApplication.class, args);
    }
}
