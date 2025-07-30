package com.safemeds.safemedsbackend.controllers.ai;

import com.safemeds.safemedsbackend.dtos.ai.AiWarningResponseDTO;
import com.safemeds.safemedsbackend.services.ai.AiWarningService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/ai/warnings")
@RequiredArgsConstructor
public class AiWarningController {

    private final AiWarningService aiWarningService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AiWarningResponseDTO>> getUserWarnings(@PathVariable UUID userId) {
        List<AiWarningResponseDTO> warnings = aiWarningService.getUserWarnings(userId);
        return ResponseEntity.ok(warnings);
    }

    @GetMapping("/user/{userId}/unseen")
    public ResponseEntity<List<AiWarningResponseDTO>> getUnseenWarnings(@PathVariable UUID userId) {
        List<AiWarningResponseDTO> warnings = aiWarningService.getUnseenWarnings(userId);
        return ResponseEntity.ok(warnings);
    }

    @PutMapping("/{warningId}/seen")
    public ResponseEntity<Void> markAsSeen(
            @PathVariable UUID warningId,
            @RequestParam UUID userId) {
        aiWarningService.markAsSeen(warningId, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{warningId}/resolved")
    public ResponseEntity<Void> markAsResolved(
            @PathVariable UUID warningId,
            @RequestParam UUID userId) {
        aiWarningService.markAsResolved(warningId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{warningId}")
    public ResponseEntity<Void> deleteWarning(
            @PathVariable UUID warningId,
            @RequestParam UUID userId) {
        aiWarningService.deleteWarning(warningId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}/urgent")
    public ResponseEntity<List<AiWarningResponseDTO>> getUrgentWarnings(@PathVariable UUID userId) {
        List<AiWarningResponseDTO> warnings = aiWarningService.getUrgentWarnings(userId);
        return ResponseEntity.ok(warnings);
    }


    @GetMapping("/user/{userId}/medicine/{medicineId}")
    public ResponseEntity<List<AiWarningResponseDTO>> getWarningsForMedicine(
            @PathVariable UUID userId,
            @PathVariable UUID medicineId) {
        List<AiWarningResponseDTO> warnings = aiWarningService.getWarningsForMedicine(userId, medicineId);
        return ResponseEntity.ok(warnings);
    }
}