package com.sulaimaan.ReminderApp.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    public void sendPushNotification(String fcmToken, String message) {
        try {
            Notification notification = Notification.builder()
                    .setTitle("Reminder")
                    .setBody(message)
                    .build();

            Message msg = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(notification)
                    .build();

            FirebaseMessaging.getInstance().send(msg);
            System.out.println("✅ Notification sent to: " + fcmToken.substring(0, 6) + "...");

        } catch (FirebaseMessagingException e) {
            System.out.println("❌ Failed to send notification: " + e.getMessage());
        }
    }
}
