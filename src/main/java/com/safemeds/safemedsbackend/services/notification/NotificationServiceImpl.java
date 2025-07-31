package com.safemeds.safemedsbackend.services.notification;

import com.safemeds.safemedsbackend.entities.Medicine;
import com.safemeds.safemedsbackend.entities.UserProfile;
import com.safemeds.safemedsbackend.entities.IntakeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    @Override
    public void sendIntakeReminder(UserProfile user, IntakeEvent intakeEvent) {
        Medicine medicine = intakeEvent.getMedicine();

        // Skip if notifications are disabled or medicine is paused
        if (!medicine.getNotificationsEnabled() || medicine.isPausedOrInactive()) {
            log.debug("Skipping notification for medicine {} - disabled or paused", medicine.getName());
            return;
        }

        String title = "Time to take your medicine";
        String message = String.format("It's time to take %s (%s)",
                medicine.getName(),
                intakeEvent.getFormattedDosage());

        // Check for low inventory warning
        if (medicine.isInventoryLow()) {
            message += " ⚠️ Low inventory remaining!";
        }

        // TODO: Integrate with your notification provider (Firebase, OneSignal, etc.)
        log.info("📱 Sending intake reminder to user {}: {}", user.getId(), message);

        // Example notification payload:
        /*
        NotificationPayload payload = NotificationPayload.builder()
            .userId(user.getId())
            .title(title)
            .message(message)
            .type("INTAKE_REMINDER")
            .data(Map.of(
                "intakeEventId", intakeEvent.getId(),
                "medicineId", medicine.getId(),
                "medicineName", medicine.getName(),
                "scheduledTime", intakeEvent.getScheduledDateTime()
            ))
            .build();

        notificationProvider.send(payload);
        */
    }

    @Override
    public void sendMissedDoseNotification(UserProfile user, IntakeEvent intakeEvent) {
        Medicine medicine = intakeEvent.getMedicine();

        if (!medicine.getNotificationsEnabled()) {
            return;
        }

        String title = "Missed dose reminder";
        String message = String.format("You missed taking %s at %s",
                medicine.getName(),
                intakeEvent.getScheduledDateTime().toLocalTime());

        // Add late intake option if still allowed
        if (intakeEvent.canStillBeTaken(medicine.getLateIntakeWindowHours())) {
            message += ". You can still take it late.";
        }

        log.info("⚠️ Sending missed dose notification to user {}: {}", user.getId(), message);
    }

    @Override
    public void sendLowInventoryNotification(UserProfile user, List<Medicine> medicines) {
        if (medicines.isEmpty()) {
            return;
        }

        String title = "Low inventory alert";
        String message;

        if (medicines.size() == 1) {
            Medicine medicine = medicines.get(0);
            message = String.format("Running low on %s (%s %s remaining)",
                    medicine.getName(),
                    medicine.getCurrentInventory(),
                    medicine.getInventoryUnit());
        } else {
            message = String.format("Running low on %d medicines. Check your inventory.", medicines.size());
        }

        log.info("📦 Sending low inventory notification to user {}: {}", user.getId(), message);
    }

    @Override
    public void sendAdherenceSummary(UserProfile user, double adherencePercentage) {
        String title = "Weekly adherence summary";
        String message = String.format("Your medication adherence this week: %.1f%%", adherencePercentage);

        if (adherencePercentage >= 90) {
            message += " 🎉 Excellent work!";
        } else if (adherencePercentage >= 70) {
            message += " 👍 Keep it up!";
        } else {
            message += " 💪 Let's improve together!";
        }

        log.info("📊 Sending adherence summary to user {}: {}", user.getId(), message);
    }

    @Override
    public void sendInteractionWarning(UserProfile user, String warningMessage) {
        String title = "Medicine interaction warning";

        log.warn("⚠️ Sending interaction warning to user {}: {}", user.getId(), warningMessage);
    }
}