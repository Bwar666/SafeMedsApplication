package com.safemeds.safemedsbackend.dtos.user;

import com.safemeds.safemedsbackend.enums.Gender;
import com.safemeds.safemedsbackend.enums.LanguagePreference;
import com.safemeds.safemedsbackend.enums.ThemePreference;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponseDTO {

    private UUID id;

    private String firstName;

    private String lastName;

    private Gender gender;

    private LocalDate birthDate;

    private LanguagePreference languagePreference;

    private ThemePreference themePreference;

    private List<AllergyDTO> allergies;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
