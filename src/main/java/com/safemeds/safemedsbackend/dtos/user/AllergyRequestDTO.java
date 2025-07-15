package com.safemeds.safemedsbackend.dtos.user;


import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllergyRequestDTO {

    @NotBlank(message = "Allergy name must not be blank")
    private String name;

    private String description;
}
