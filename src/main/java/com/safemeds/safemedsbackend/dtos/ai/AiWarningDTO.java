package com.safemeds.safemedsbackend.dtos.ai;

import com.safemeds.safemedsbackend.enums.AiWarningType;
import com.safemeds.safemedsbackend.enums.WarningSeverity;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiWarningDTO {

    private UUID id;

    private AiWarningType type;

    private WarningSeverity severity;

    private String message;

    private String details;

    private boolean seen;

    private UUID userProfileId;

    private Set<UUID> relatedMedicineIds;

    private LocalDateTime resolvedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
