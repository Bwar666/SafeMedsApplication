package com.safemeds.safemedsbackend.dtos.medicine;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntakeScheduleDTO {
    @NotNull
    @JsonFormat(pattern = "HH:mm")
    private LocalTime time;

    @Positive
    private Double amount;
}

