package com.safemeds.safemedsbackend.controllers.medicine;

import com.safemeds.safemedsbackend.dtos.medicine.*;
import com.safemeds.safemedsbackend.services.medicine.MedicineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users/{userId}/medicines")
@RequiredArgsConstructor
public class MedicineController {

    private final MedicineService medicineService;

    @PostMapping
    public ResponseEntity<MedicineResponseDTO> createMedicine(
            @PathVariable UUID userId,
            @Valid @RequestBody MedicineRequestDTO requestDTO) {
        return ResponseEntity.ok(medicineService.createMedicine(userId, requestDTO));
    }

    @GetMapping
    public ResponseEntity<List<MedicineResponseDTO>> getAllMedicines(
            @PathVariable UUID userId) {
        return ResponseEntity.ok(medicineService.getAllMedicinesByUser(userId));
    }

    @GetMapping("/{medicineId}")
    public ResponseEntity<MedicineResponseDTO> getMedicineById(
            @PathVariable UUID userId,
            @PathVariable UUID medicineId) {
        return ResponseEntity.ok(medicineService.getMedicineById(userId, medicineId));
    }

    @PutMapping("/{medicineId}")
    public ResponseEntity<MedicineResponseDTO> updateMedicine(
            @PathVariable UUID userId,
            @PathVariable UUID medicineId,
            @Valid @RequestBody MedicineRequestDTO requestDTO) {
        return ResponseEntity.ok(medicineService.updateMedicine(userId, medicineId, requestDTO));
    }

    @DeleteMapping("/{medicineId}")
    public ResponseEntity<Void> deleteMedicine(
            @PathVariable UUID userId,
            @PathVariable UUID medicineId) {
        medicineService.deleteMedicine(userId, medicineId);
        return ResponseEntity.noContent().build();
    }
}