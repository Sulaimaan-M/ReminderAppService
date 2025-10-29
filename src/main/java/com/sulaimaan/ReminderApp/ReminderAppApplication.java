package com.sulaimaan.ReminderApp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for the Reminder App
 */
@SpringBootApplication
public class ReminderAppApplication {

    private static final Logger logger = LoggerFactory.getLogger(ReminderAppApplication.class);

    /**
     * Application entry point that bootstraps the Spring Boot application
     */
    public static void main(String[] args) {
        logger.info("Starting ReminderApp Application...");
        SpringApplication.run(ReminderAppApplication.class, args);
        logger.info("ReminderApp Application started successfully");
    }

}
