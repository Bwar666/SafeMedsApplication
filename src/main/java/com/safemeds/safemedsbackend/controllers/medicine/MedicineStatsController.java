package com.safemeds.safemedsbackend.controllers.medicine;

import com.safemeds.safemedsbackend.dtos.medicine.DailyMedicineScheduleDTO;
import com.safemeds.safemedsbackend.services.medicine.MedicineUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
//ENDPOINTS FOR COMPREHENSIVE MEDICINE MANAGEMENT
@RestController
@RequestMapping("/api/users/{userId}/medicines/stats")
@RequiredArgsConstructor
class MedicineStatsController {

    private final MedicineUsageService medicineUsageService;

    @GetMapping("/today")
    public ResponseEntity<DailyMedicineScheduleDTO> getTodayStats(@PathVariable UUID userId) {
        LocalDate today = LocalDate.now();
        return ResponseEntity.ok(medicineUsageService.getDailySchedule(userId, today));
    }

    @GetMapping("/this-week")
    public ResponseEntity<List<DailyMedicineScheduleDTO>> getThisWeekStats(@PathVariable UUID userId) {
        LocalDate startOfWeek = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        return ResponseEntity.ok(medicineUsageService.getWeeklySchedule(userId, startOfWeek));
    }
}