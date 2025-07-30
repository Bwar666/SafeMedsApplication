package com.safemeds.safemedsbackend.dtos.user;


import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllergyRequestDTO {
    private UUID id;

    @NotBlank(message = "Allergy name must not be blank")
    private String name;

    private String description;
}
