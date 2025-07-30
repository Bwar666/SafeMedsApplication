package com.safemeds.safemedsbackend.services.notification;

import com.safemeds.safemedsbackend.entities.Medicine;
import com.safemeds.safemedsbackend.entities.UserProfile;
import com.safemeds.safemedsbackend.entities.IntakeEvent;
import java.util.List;

public interface NotificationService {

    void sendIntakeReminder(UserProfile user, IntakeEvent intakeEvent);

    void sendMissedDoseNotification(UserProfile user, IntakeEvent intakeEvent);

    void sendLowInventoryNotification(UserProfile user, List<Medicine> medicines);

    void sendAdherenceSummary(UserProfile user, double adherencePercentage);

    void sendInteractionWarning(UserProfile user, String warningMessage);
}