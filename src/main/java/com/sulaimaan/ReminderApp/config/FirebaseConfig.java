package com.sulaimaan.ReminderApp.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration
public class FirebaseConfig {

    // In your main Application class or a @Configuration class
    @PostConstruct
    public void initFirebase() {
        try {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(
                            Objects.requireNonNull(getClass().getResourceAsStream("/firebase-service-account.json"))
                    ))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("✅ Firebase initialized");
            }
        } catch (Exception e) {
            System.err.println("❌ Firebase init failed: " + e.getMessage());
        }
    }

}
