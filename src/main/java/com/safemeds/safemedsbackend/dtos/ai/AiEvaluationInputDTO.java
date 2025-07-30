package com.safemeds.safemedsbackend.dtos.ai;

import com.safemeds.safemedsbackend.enums.AiWarningType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class AiEvaluationInputDTO {
    private String medicineName;
    private AiWarningType riskType;

    private String allergyName;
    private String targetMedicineName;
    private List<String> userMedicines;
    private List<String> userAllergies;
    private Double dosageAmount;
    private String dosageUnit;
    private List<LocalTime> scheduledTimes;
    private String foodInstructions;
}