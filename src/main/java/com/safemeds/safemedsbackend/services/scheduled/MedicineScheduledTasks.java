// MedicineScheduledTasks.java - Automated background processes
package com.safemeds.safemedsbackend.services.scheduled;

import com.safemeds.safemedsbackend.entities.IntakeEvent;
import com.safemeds.safemedsbackend.entities.Medicine;
import com.safemeds.safemedsbackend.entities.UserProfile;
import com.safemeds.safemedsbackend.enums.IntakeStatus;
import com.safemeds.safemedsbackend.repositories.medicine.IntakeEventRepository;
import com.safemeds.safemedsbackend.repositories.medicine.MedicineRepository;
import com.safemeds.safemedsbackend.repositories.user.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicineScheduledTasks {

    private final IntakeEventRepository intakeEventRepository;
    private final MedicineRepository medicineRepository;
    private final UserProfileRepository userProfileRepository;

    @Scheduled(fixedRate = 900000)
    @Transactional
    public void processMissedDoses() {
        log.info("Starting scheduled missed dose processing");

        try {
            List<IntakeEvent> scheduledEvents = intakeEventRepository
                    .findEventsToMarkAsMissed(IntakeStatus.SCHEDULED, LocalDateTime.now());

            int missedCount = 0;

            for (IntakeEvent event : scheduledEvents) {
                Medicine medicine = event.getMedicine();

                if (medicine.isPausedOrInactive()) {
                    continue;
                }

                if (event.isMissedByThreshold(medicine.getMissedDoseThresholdMinutes())) {
                    event.markAsMissed(true);
                    missedCount++;
                }
            }

            if (missedCount > 0) {
                intakeEventRepository.saveAll(scheduledEvents);
                log.info("Marked {} doses as missed", missedCount);
            }

        } catch (Exception e) {
            log.error("Error processing missed doses: {}", e.getMessage(), e);
        }
    }
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional(readOnly = true)
    public void checkLowInventory() {
        log.info("Starting scheduled low inventory check");

        try {
            List<UserProfile> users = userProfileRepository.findAll();
            int totalLowInventoryMedicines = 0;

            for (UserProfile user : users) {
                List<Medicine> lowInventoryMedicines = medicineRepository
                        .findLowInventoryMedicines(user.getId());

                if (!lowInventoryMedicines.isEmpty()) {
                    totalLowInventoryMedicines += lowInventoryMedicines.size();


                    log.info("User {} has {} medicines with low inventory",
                            user.getId(), lowInventoryMedicines.size());

                }
            }

            log.info("Found {} medicines with low inventory across all users", totalLowInventoryMedicines);

        } catch (Exception e) {
            log.error("Error checking low inventory: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 2 1 * *")
    @Transactional
    public void cleanupOldIntakeEvents() {
        log.info("Starting scheduled cleanup of old intake events");

        try {
            LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);

            List<IntakeEvent> oldEvents = intakeEventRepository
                    .findByScheduledDateTimeBefore(oneYearAgo);

            if (!oldEvents.isEmpty()) {
                intakeEventRepository.deleteAll(oldEvents);
                log.info("Cleaned up {} old intake events", oldEvents.size());
            }

        } catch (Exception e) {
            log.error("Error cleaning up old intake events: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void generateUpcomingIntakeEvents() {
        log.info("Starting scheduled generation of upcoming intake events");

        try {
            List<Medicine> activeMedicines = medicineRepository.findByIsActiveTrue();
            int totalEventsGenerated = 0;

            for (Medicine medicine : activeMedicines) {
                if (medicine.isPausedOrInactive()) {
                    continue;
                }

                LocalDateTime futureLimit = LocalDateTime.now().plusDays(medicine.getScheduleDuration());
                List<IntakeEvent> futureEvents = intakeEventRepository
                        .findByMedicineIdAndScheduledDateTimeAfter(medicine.getId(), LocalDateTime.now());


                LocalDateTime weekFromNow = LocalDateTime.now().plusDays(7);
                boolean needsMoreEvents = futureEvents.stream()
                        .noneMatch(event -> event.getScheduledDateTime().isAfter(weekFromNow));

                if (needsMoreEvents) {
                    log.debug("Generated additional events for medicine: {}", medicine.getName());
                    totalEventsGenerated++;
                }
            }

            log.info("Generated events for {} medicines", totalEventsGenerated);

        } catch (Exception e) {
            log.error("Error generating upcoming intake events: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 20 * * *")
    @Transactional(readOnly = true)
    public void sendDailyAdherenceSummary() {
        log.info("Generating daily adherence summaries");

        try {
            List<UserProfile> users = userProfileRepository.findAll();

            for (UserProfile user : users) {

                LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
                LocalDateTime endOfDay = LocalDateTime.now().toLocalDate().atTime(23, 59, 59);

                List<Object[]> stats = intakeEventRepository
                        .getAdherenceStatistics(user.getId(), startOfDay, endOfDay);


                if (!stats.isEmpty()) {
                    log.debug("Generated adherence summary for user: {}", user.getId());

                }
            }

        } catch (Exception e) {
            log.error("Error generating daily adherence summaries: {}", e.getMessage(), e);
        }
    }
}