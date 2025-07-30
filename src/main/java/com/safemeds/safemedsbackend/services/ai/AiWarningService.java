package com.safemeds.safemedsbackend.services.ai;

import com.safemeds.safemedsbackend.dtos.ai.AiWarningResponseDTO;
import com.safemeds.safemedsbackend.entities.Medicine;

import java.util.List;
import java.util.UUID;

public interface AiWarningService {
    List<AiWarningResponseDTO> evaluateAndCreateWarnings(UUID userId, Medicine medicine);
    List<AiWarningResponseDTO> getUserWarnings(UUID userId);
    List<AiWarningResponseDTO> getUnseenWarnings(UUID userId);
    void markAsSeen(UUID warningId, UUID userId);
    void markAsResolved(UUID warningId, UUID userId);
    void deleteWarning(UUID warningId, UUID userId);
    List<AiWarningResponseDTO> getUrgentWarnings(UUID userId);
    List<AiWarningResponseDTO> getWarningsForMedicine(UUID userId, UUID medicineId);
}