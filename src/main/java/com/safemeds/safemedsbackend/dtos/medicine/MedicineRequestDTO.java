package com.safemeds.safemedsbackend.dtos.medicine;

import com.safemeds.safemedsbackend.enums.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicineRequestDTO {

    @NotBlank(message = "Medicine name is required")
    private String name;

    @NotNull(message = "Medicine form is required")
    private MedicineForm form;

    // Optional reason for taking the medicine (e.g., "Headache")
    private String conditionReason;

    @NotNull(message = "Frequency type is required")
    private FrequencyType frequencyType;

    // Format depends on frequencyType — e.g., specific days, recurring intervals, etc.
    private String frequencyDetails;

    @NotNull(message = "Intake pattern is required")
    private IntakePattern intakePattern;

    // Intake times like ["08:00", "14:00", "20:00"]
    @NotEmpty(message = "At least one intake time is required")
    private List<@Pattern(regexp = "^\\d{2}:\\d{2}$", message = "Time must be in HH:mm format") String> intakeTimes;

    @Min(value = 1, message = "Dosage must be at least 1")
    private int dosageAmount;

    // Optional
    private Integer refillReminderThreshold;

    // Optional (e.g., BEFORE_FOOD, WITH_FOOD)
    private FoodInstruction foodInstruction;

    private List<IntakeScheduleDTO> intakeSchedules;

    // Optional UI icon ID or name
    private String icon;

    // Optional color code (e.g., "#F44336")
    private String color;

    @NotNull(message = "isActive flag is required")
    private Boolean isActive;
}
