package com.safemeds.safemedsbackend.dtos.medicine;


import com.safemeds.safemedsbackend.enums.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicineResponseDTO {

    private UUID id;

    private String name;
    private MedicineForm form;
    private String conditionReason;
    private FrequencyType frequencyType;
    private String frequencyDetails;
    private IntakePattern intakePattern;
    private List<String> intakeTimes;
    private int dosageAmount;
    private Integer refillReminderThreshold;
    private FoodInstruction foodInstruction;
    private String icon;
    private String color;
    private Boolean isActive;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
