package com.safemeds.safemedsbackend.dtos.user;


import com.safemeds.safemedsbackend.enums.Gender;
import com.safemeds.safemedsbackend.enums.LanguagePreference;
import com.safemeds.safemedsbackend.enums.ThemePreference;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileRequestDTO {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotNull(message = "Birth date is required")
    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;

    @NotNull(message = "Language preference is required")
    private LanguagePreference languagePreference;

    @NotNull(message = "Theme preference is required")
    private ThemePreference themePreference;

    /**
     * List of allergy IDs the user selects (from the master list).
     * Frontend should send selected UUIDs only.
     */
    private List<UUID> allergyIds;
}
