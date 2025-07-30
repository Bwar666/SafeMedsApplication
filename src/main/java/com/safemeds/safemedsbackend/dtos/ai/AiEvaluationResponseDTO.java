package com.safemeds.safemedsbackend.dtos.ai;

import com.safemeds.safemedsbackend.enums.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiEvaluationResponseDTO {
    private UUID id;
    private String medicineName;
    private AiWarningType riskType;
    private String allergyName;
    private String targetMedicineName;
    private String message;
    private WarningSeverity severity;
    private AiWarningStatus status;
    private AiWarningSource source;
    private String details;
    private LocalDateTime createdAt;
}