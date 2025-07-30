package com.safemeds.safemedsbackend.controllers.medicine;

import com.safemeds.safemedsbackend.services.medicine.MedicineUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//ENDPOINT FOR SYSTEM MAINTENANCE (Optional - Admin only)
@RestController
@RequestMapping("/api/admin/medicines")
@RequiredArgsConstructor
class MedicineMaintenanceController {

    private final MedicineUsageService medicineUsageService;

    @PostMapping("/process-all-missed")
    public ResponseEntity<Void> processAllMissedDoses() {
        return ResponseEntity.ok().build();
    }
}