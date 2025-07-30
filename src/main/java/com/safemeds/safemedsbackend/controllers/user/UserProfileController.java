package com.safemeds.safemedsbackend.controllers.user;


import com.safemeds.safemedsbackend.dtos.user.AllergyRequestDTO;
import com.safemeds.safemedsbackend.dtos.user.UserProfileRequestDTO;
import com.safemeds.safemedsbackend.dtos.user.UserProfileResponseDTO;
import com.safemeds.safemedsbackend.mappers.user.UserProfileMapper;
import com.safemeds.safemedsbackend.repositories.user.UserProfileRepository;
import com.safemeds.safemedsbackend.services.user.UserProfileService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserProfileController {
    private final UserProfileRepository userProfileRepository;
    private final UserProfileMapper userProfileMapper;
    private final UserProfileService userProfileService;

    @PostMapping("/create")
    public ResponseEntity<UserProfileResponseDTO> createUserProfile(
            @Valid @RequestBody UserProfileRequestDTO requestDTO) {

        UserProfileResponseDTO createdUser = userProfileService.createProfile(requestDTO);
        return ResponseEntity.ok(createdUser);
    }


    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponseDTO> getUserProfileById(@PathVariable UUID id) {
        UserProfileResponseDTO user = userProfileService.getProfileById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<UserProfileResponseDTO> updateUserProfile(
            @PathVariable UUID id,
            @Valid @RequestBody UserProfileRequestDTO requestDTO) {

        UserProfileResponseDTO updatedUser = userProfileService.updateProfile(id, requestDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/all")
    public List<UserProfileResponseDTO> getAllUsers(){
        return userProfileRepository.findAll()
                .stream()
                .map(userProfileMapper::toResponseDTO)
                .toList();
    }

    @PutMapping("/{id}/allergies")
    public ResponseEntity<UserProfileResponseDTO> updateUserAllergies(
            @PathVariable UUID id,
            @Valid @RequestBody List<AllergyRequestDTO> allergies) {

        UserProfileResponseDTO updated = userProfileService.updateAllergies(id, allergies);
        return ResponseEntity.ok(updated);
    }
}
