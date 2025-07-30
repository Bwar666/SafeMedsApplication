package com.safemeds.safemedsbackend.dtos.medicine;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TakeMedicineRequestDTO {
    @NotNull(message = "Intake event ID is required")
    private UUID intakeEventId;

    private LocalDateTime actualTakeTime;

    @Positive(message = "Dosage amount must be positive")
    private Double actualDosageAmount;

    private String note;

    private Boolean deductFromInventory = true;
}