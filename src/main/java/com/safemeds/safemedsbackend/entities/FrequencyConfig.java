// FrequencyConfig.java
package com.safemeds.safemedsbackend.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.util.Set;


@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FrequencyConfig {

    private Integer intervalDays;
    private Integer dayOfMonth;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "medicine_specific_days",
            joinColumns = @JoinColumn(name = "medicine_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    private Set<DayOfWeek> specificDays;

    private Integer cycleActiveDays;
    private Integer cycleRestDays;

}