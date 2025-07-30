// MedicineUsageController.java
package com.safemeds.safemedsbackend.controllers.medicine;

import com.safemeds.safemedsbackend.dtos.medicine.*;
import com.safemeds.safemedsbackend.services.medicine.MedicineUsageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users/{userId}/medicines")
@RequiredArgsConstructor
public class MedicineUsageController {

    private final MedicineUsageService medicineUsageService;


    @PostMapping("/take")
    public ResponseEntity<MedicineUsageResponseDTO> takeMedicine(
            @PathVariable UUID userId,
            @Valid @RequestBody TakeMedicineRequestDTO request) {
        MedicineUsageResponseDTO response = medicineUsageService.takeMedicine(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/skip")
    public ResponseEntity<MedicineUsageResponseDTO> skipMedicine(
            @PathVariable UUID userId,
            @Valid @RequestBody SkipMedicineRequestDTO request) {
        MedicineUsageResponseDTO response = medicineUsageService.skipMedicine(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{medicineId}/pause")
    public ResponseEntity<Void> pauseMedicine(
            @PathVariable UUID userId,
            @PathVariable UUID medicineId,
            @Valid @RequestBody PauseMedicineRequestDTO request) {
        medicineUsageService.pauseMedicine(userId, medicineId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{medicineId}/resume")
    public ResponseEntity<Void> resumeMedicine(
            @PathVariable UUID userId,
            @PathVariable UUID medicineId) {
        medicineUsageService.resumeMedicine(userId, medicineId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{medicineId}/notifications")
    public ResponseEntity<Void> toggleNotifications(
            @PathVariable UUID userId,
            @PathVariable UUID medicineId,
            @RequestParam boolean enabled) {
        medicineUsageService.toggleNotifications(userId, medicineId, enabled);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/schedule/daily")
    public ResponseEntity<DailyMedicineScheduleDTO> getDailySchedule(
            @PathVariable UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        DailyMedicineScheduleDTO schedule = medicineUsageService.getDailySchedule(userId, date);
        return ResponseEntity.ok(schedule);
    }

    @GetMapping("/schedule/weekly")
    public ResponseEntity<List<DailyMedicineScheduleDTO>> getWeeklySchedule(
            @PathVariable UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        List<DailyMedicineScheduleDTO> schedule = medicineUsageService.getWeeklySchedule(userId, startDate);
        return ResponseEntity.ok(schedule);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<IntakeEventDTO>> getUpcomingIntakes(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "24") int hours) {
        List<IntakeEventDTO> intakes = medicineUsageService.getUpcomingIntakes(userId, hours);
        return ResponseEntity.ok(intakes);
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<IntakeEventDTO>> getOverdueIntakes(
            @PathVariable UUID userId) {
        List<IntakeEventDTO> overdue = medicineUsageService.getOverdueIntakes(userId);
        return ResponseEntity.ok(overdue);
    }


    @PutMapping("/{medicineId}/inventory")
    public ResponseEntity<Void> updateInventory(
            @PathVariable UUID userId,
            @PathVariable UUID medicineId,
            @Valid @RequestBody InventoryUpdateRequestDTO request) {
        medicineUsageService.updateInventory(userId, medicineId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/low-inventory")
    public ResponseEntity<List<MedicineResponseDTO>> getLowInventoryMedicines(
            @PathVariable UUID userId) {
        List<MedicineResponseDTO> medicines = medicineUsageService.getLowInventoryMedicines(userId);
        return ResponseEntity.ok(medicines);
    }


    @PostMapping("/process-missed")
    public ResponseEntity<Void> processMissedDoses(
            @PathVariable UUID userId) {
        medicineUsageService.processMissedDoses(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/events/{intakeEventId}/mark-missed")
    public ResponseEntity<Void> markEventAsMissed(
            @PathVariable UUID userId,
            @PathVariable UUID intakeEventId,
            @RequestParam(defaultValue = "false") boolean automatic) {
        medicineUsageService.markEventAsMissed(userId, intakeEventId, automatic);
        return ResponseEntity.ok().build();
    }
}
