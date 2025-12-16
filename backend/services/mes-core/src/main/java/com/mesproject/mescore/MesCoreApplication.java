package com.mesproject.mescore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MesCoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(MesCoreApplication.class, args);
    }
}
