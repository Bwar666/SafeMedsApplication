package com.safemeds.safemedsbackend.controllers.notification;

import com.safemeds.safemedsbackend.services.medicine.MedicineUsageService;
import com.safemeds.safemedsbackend.services.notification.NotificationScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationScheduler notificationScheduler;
    private final MedicineUsageService medicineUsageService;

    /**
     * Manual trigger for processing missed doses (useful for testing or manual runs)
     */
    @PostMapping("/process-missed")
    public ResponseEntity<String> processMissedDoses() {
        try {
            notificationScheduler.checkMissedDoses();
            return ResponseEntity.ok("Missed doses processed successfully");
        } catch (Exception e) {
            log.error("Error processing missed doses: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Error processing missed doses");
        }
    }

    /**
     * Manual trigger for checking upcoming intakes (useful for testing)
     */
    @PostMapping("/check-upcoming")
    public ResponseEntity<String> checkUpcomingIntakes() {
        try {
            notificationScheduler.checkUpcomingIntakes();
            return ResponseEntity.ok("Upcoming intakes checked successfully");
        } catch (Exception e) {
            log.error("Error checking upcoming intakes: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Error checking upcoming intakes");
        }
    }

    /**
     * Manual trigger for low inventory check (useful for testing)
     */
    @PostMapping("/check-inventory")
    public ResponseEntity<String> checkLowInventory() {
        try {
            notificationScheduler.checkLowInventory();
            return ResponseEntity.ok("Low inventory check completed successfully");
        } catch (Exception e) {
            log.error("Error checking low inventory: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Error checking low inventory");
        }
    }

    /**
     * Process missed doses for a specific user
     */
    @PostMapping("/users/{userId}/process-missed")
    public ResponseEntity<String> processUserMissedDoses(@PathVariable UUID userId) {
        try {
            medicineUsageService.processMissedDoses(userId);
            return ResponseEntity.ok("User missed doses processed successfully");
        } catch (Exception e) {
            log.error("Error processing missed doses for user {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError().body("Error processing user missed doses");
        }
    }
}