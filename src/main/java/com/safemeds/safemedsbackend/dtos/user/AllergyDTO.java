package com.safemeds.safemedsbackend.dtos.user;


import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllergyDTO {

    // Unique identifier for allergy (used in response)
    private UUID id;

    // Name of the allergy (e.g., "Peanuts", "Penicillin")
    private String name;

    // Optional description provided by user (e.g., "Causes rash and breathing issues")
    private String description;
}
