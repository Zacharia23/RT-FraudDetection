package com.example.frauddetector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FrauddetectorApplication {

    public static void main(String[] args) {
        SpringApplication.run(FrauddetectorApplication.class, args);
    }

}
