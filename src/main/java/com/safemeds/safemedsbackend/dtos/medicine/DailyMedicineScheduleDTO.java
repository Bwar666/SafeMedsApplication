package com.safemeds.safemedsbackend.dtos.medicine;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyMedicineScheduleDTO {
    private LocalDate date;
    private List<IntakeEventDTO> intakeEvents;
    private Integer totalScheduled;
    private Integer totalTaken;
    private Integer totalSkipped;
    private Integer totalMissed;
    private Integer totalPending;
}