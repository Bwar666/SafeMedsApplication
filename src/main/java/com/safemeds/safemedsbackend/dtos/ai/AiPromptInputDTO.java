package com.safemeds.safemedsbackend.dtos.ai;

import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class AiPromptInputDTO {
    private String medicineName;
    private String allergyName;
    private Double dosageAmount;
    private String foodInstruction;

    private List<String> otherMedicines;
    private List<LocalTime> conflictingTimes;
    private String conditionReason;
    private String medicineForm;
}