package com.nexus.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AiAnalyticsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiAnalyticsServiceApplication.class, args);
    }
}
