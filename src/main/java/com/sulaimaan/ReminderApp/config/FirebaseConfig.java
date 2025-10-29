package com.sulaimaan.ReminderApp.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

/**
 * Configuration class for initializing Firebase Admin SDK
 */
@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    /**
     * Initializes Firebase Admin SDK with service account credentials on application startup
     */
    @PostConstruct
    public void initFirebase() {
        try {
            logger.info("Starting Firebase initialization");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(
                            Objects.requireNonNull(getClass().getResourceAsStream("/firebase-service-account.json"))
                    ))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                logger.info("Firebase initialized successfully");
            } else {
                logger.info("Firebase already initialized, skipping initialization");
            }
        } catch (Exception e) {
            logger.error("Firebase initialization failed: {}", e.getMessage(), e);
        }
    }
}
