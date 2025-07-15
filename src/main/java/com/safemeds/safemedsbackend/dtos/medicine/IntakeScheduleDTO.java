package com.safemeds.safemedsbackend.dtos.medicine;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntakeScheduleDTO {

    @NotBlank(message = "Time is required (in HH:mm format)")
    @Pattern(regexp = "^\\d{2}:\\d{2}$", message = "Time must be in HH:mm format")
    private String time; // e.g., "08:00"

    @Min(value = 1, message = "Dosage amount must be at least 1")
    private int dosageAmount;
}
