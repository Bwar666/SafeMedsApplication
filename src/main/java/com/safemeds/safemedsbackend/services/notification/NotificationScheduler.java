package com.safemeds.safemedsbackend.services.notification;

import com.safemeds.safemedsbackend.entities.IntakeEvent;
import com.safemeds.safemedsbackend.entities.Medicine;
import com.safemeds.safemedsbackend.entities.UserProfile;
import com.safemeds.safemedsbackend.enums.IntakeStatus;
import com.safemeds.safemedsbackend.repositories.medicine.IntakeEventRepository;
import com.safemeds.safemedsbackend.repositories.medicine.MedicineRepository;
import com.safemeds.safemedsbackend.services.medicine.MedicineUsageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final IntakeEventRepository intakeEventRepository;
    private final MedicineRepository medicineRepository;
    private final NotificationService notificationService;
    private final MedicineUsageService medicineUsageService;

    // Check for upcoming intake reminders every 5 minutes
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void checkUpcomingIntakes() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderWindow = now.plusMinutes(10); // Notify 10 minutes before

        List<IntakeEvent> upcomingEvents = intakeEventRepository
                .findByStatusAndScheduledDateTimeBetween(
                        IntakeStatus.SCHEDULED,
                        now.plusMinutes(9), // Between 9-10 minutes from now
                        reminderWindow
                );

        log.debug("Found {} upcoming intake events to notify", upcomingEvents.size());

        for (IntakeEvent event : upcomingEvents) {
            Medicine medicine = event.getMedicine();

            // Skip if medicine is paused/inactive or notifications disabled
            if (medicine.isPausedOrInactive() || !medicine.getNotificationsEnabled()) {
                continue;
            }

            UserProfile user = medicine.getUserProfile();
            notificationService.sendIntakeReminder(user, event);
        }
    }

    // Check for missed doses every 30 minutes
    @Scheduled(fixedRate = 1800000) // 30 minutes
    public void checkMissedDoses() {
        log.debug("Checking for missed doses...");

        List<IntakeEvent> scheduledEvents = intakeEventRepository
                .findByStatus(IntakeStatus.SCHEDULED);

        LocalDateTime now = LocalDateTime.now();
        int processedCount = 0;

        // Group events by user for efficient processing
        Map<UserProfile, List<IntakeEvent>> eventsByUser = scheduledEvents.stream()
                .collect(Collectors.groupingBy(event -> event.getMedicine().getUserProfile()));

        for (Map.Entry<UserProfile, List<IntakeEvent>> entry : eventsByUser.entrySet()) {
            UserProfile user = entry.getKey();
            List<IntakeEvent> userEvents = entry.getValue();

            for (IntakeEvent event : userEvents) {
                Medicine medicine = event.getMedicine();

                // Skip paused/inactive medicines
                if (medicine.isPausedOrInactive()) {
                    continue;
                }

                // Check if event should be marked as missed
                if (event.isMissedByThreshold(medicine.getMissedDoseThresholdMinutes())) {
                    try {
                        medicineUsageService.markEventAsMissed(user.getId(), event.getId(), true);
                        notificationService.sendMissedDoseNotification(user, event);
                        processedCount++;
                    } catch (Exception e) {
                        log.error("Failed to mark event {} as missed for user {}: {}",
                                event.getId(), user.getId(), e.getMessage());
                    }
                }
            }
        }

        if (processedCount > 0) {
            log.info("Processed {} missed doses", processedCount);
        }
    }

    // Check for low inventory daily at 9 AM
    @Scheduled(cron = "0 0 9 * * *")
    public void checkLowInventory() {
        log.info("Running daily low inventory check...");

        List<Medicine> activeMedicines = medicineRepository.findByIsActiveTrueAndNotificationsEnabledTrue();

        // Group medicines by user
        Map<UserProfile, List<Medicine>> medicinesByUser = activeMedicines.stream()
                .filter(Medicine::isInventoryLow)
                .collect(Collectors.groupingBy(Medicine::getUserProfile));

        for (Map.Entry<UserProfile, List<Medicine>> entry : medicinesByUser.entrySet()) {
            UserProfile user = entry.getKey();
            List<Medicine> lowInventoryMedicines = entry.getValue();

            if (!lowInventoryMedicines.isEmpty()) {
                notificationService.sendLowInventoryNotification(user, lowInventoryMedicines);
            }
        }

        log.info("Low inventory check completed for {} users", medicinesByUser.size());
    }

    // Send weekly adherence summary on Sundays at 8 PM
    @Scheduled(cron = "0 0 20 * * SUN")
    public void sendWeeklyAdherenceSummary() {
        log.info("Sending weekly adherence summaries...");

        List<UserProfile> activeUsers = medicineRepository.findDistinctUsersByActiveMedicines();

        for (UserProfile user : activeUsers) {
            try {
                double adherence = calculateWeeklyAdherence(user);
                notificationService.sendAdherenceSummary(user, adherence);
            } catch (Exception e) {
                log.error("Failed to send adherence summary to user {}: {}", user.getId(), e.getMessage());
            }
        }

        log.info("Weekly adherence summaries sent to {} users", activeUsers.size());
    }

    private double calculateWeeklyAdherence(UserProfile user) {
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
        LocalDateTime weekEnd = LocalDateTime.now();

        List<IntakeEvent> weekEvents = intakeEventRepository
                .findByMedicineUserProfileIdAndScheduledDateTimeBetween(
                        user.getId(), weekStart, weekEnd);

        if (weekEvents.isEmpty()) {
            return 100.0; // No events = perfect adherence
        }

        long totalEvents = weekEvents.size();
        long takenEvents = weekEvents.stream()
                .filter(event -> event.getStatus() == IntakeStatus.TAKEN)
                .count();

        return (double) takenEvents / totalEvents * 100.0;
    }
}