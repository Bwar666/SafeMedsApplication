package com.safemeds.safemedsbackend.dtos.medicine;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FrequencyConfigDTO {
    private Integer intervalDays;
    private Set<DayOfWeek> specificDays;
    private Integer cycleActiveDays;
    private Integer cycleRestDays;
    private Integer dayOfMonth;
}

