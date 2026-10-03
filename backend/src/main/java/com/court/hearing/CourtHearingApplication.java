package com.court.hearing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CourtHearingApplication {
    public static void main(String[] args) {
        SpringApplication.run(CourtHearingApplication.class, args);
    }
}
