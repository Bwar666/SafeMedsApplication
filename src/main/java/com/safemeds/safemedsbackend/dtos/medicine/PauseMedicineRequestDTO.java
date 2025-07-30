package com.safemeds.safemedsbackend.dtos.medicine;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PauseMedicineRequestDTO {
    @NotBlank(message = "Pause reason is required")
    private String pauseReason;

    private LocalDateTime resumeAt;

    private Boolean pauseNotifications = true;
}