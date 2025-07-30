package com.safemeds.safemedsbackend.dtos.medicine;


import com.safemeds.safemedsbackend.dtos.user.AllergyResponseDTO;
import com.safemeds.safemedsbackend.enums.*;
import lombok.*;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineResponseDTO {
    private UUID id;
    private String name;
    private MedicineForm form;
    private String conditionReason;
    private FrequencyType frequencyType;
    private List<LocalTime> intakeTimes;
    private FrequencyConfigDTO frequencyConfig;
    private List<IntakeScheduleDTO> intakeSchedules;
    private Integer scheduleDuration;
    private Integer refillReminderThreshold;
    private FoodInstruction foodInstruction;
    private String icon;
    private String color;
    private boolean isActive;
    private List<AllergyResponseDTO> relatedAllergies;
    private String formattedDosage;
}

