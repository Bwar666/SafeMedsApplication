package com.safemeds.safemedsbackend.dtos.ai;

import com.safemeds.safemedsbackend.enums.AiWarningType;
import com.safemeds.safemedsbackend.enums.WarningSeverity;
import lombok.*;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StructuredAiResponseDTO {
    private AiWarningType type;
    private WarningSeverity severity;
    private String message;
    private Map<String, String> details;
}
