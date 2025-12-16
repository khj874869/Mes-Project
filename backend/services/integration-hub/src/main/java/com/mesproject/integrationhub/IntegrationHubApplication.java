package com.mesproject.integrationhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class IntegrationHubApplication {
    public static void main(String[] args) {
        SpringApplication.run(IntegrationHubApplication.class, args);
    }
}
