package com.safemeds.safemedsbackend.dtos.medicine;

import com.safemeds.safemedsbackend.enums.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineRequestDTO {
    @NotBlank(message = "Medicine name is required")
    private String name;

    @NotNull(message = "Medicine form is required")
    private MedicineForm form;

    private String conditionReason;

    @NotNull(message = "Frequency type is required")
    private FrequencyType frequencyType;

    private FrequencyConfigDTO frequencyConfig;

    @NotEmpty(message = "At least one intake time is required")
    private List<LocalTime> intakeTimes;

    @NotEmpty(message = "At least one intake schedule is required")
    private List<IntakeScheduleDTO> intakeSchedules;

    private Integer scheduleDuration;

    private Set<UUID> relatedAllergyIds;

    private Integer refillReminderThreshold;

    private FoodInstruction foodInstruction;

    private String icon;
    private String color;
}

