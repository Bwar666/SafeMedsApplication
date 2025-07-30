package com.safemeds.safemedsbackend.dtos.ai;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiWarningResponseDTO {
    private UUID id;
    private boolean seen;
    private boolean resolved;
    private String medicineName;
    private String allergyName;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private AiEvaluationResponseDTO evaluationResult;
}