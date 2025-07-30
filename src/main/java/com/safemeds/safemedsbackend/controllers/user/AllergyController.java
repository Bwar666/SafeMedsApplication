package com.safemeds.safemedsbackend.controllers.user;


import com.safemeds.safemedsbackend.dtos.user.AllergyRequestDTO;
import com.safemeds.safemedsbackend.dtos.user.AllergyResponseDTO;
import com.safemeds.safemedsbackend.services.user.AllergyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users/{userId}/allergies")
@RequiredArgsConstructor
public class AllergyController {

    private final AllergyService allergyService;

    @PostMapping("/create")
    public ResponseEntity<AllergyResponseDTO> createAllergy(
            @PathVariable UUID userId,
            @RequestBody AllergyRequestDTO requestDTO) {
        return ResponseEntity.ok(allergyService.createAllergy(userId, requestDTO));
    }

    @GetMapping("{allergyId}")
    public  ResponseEntity<AllergyResponseDTO> getAllergyById(
            @PathVariable UUID allergyId,
            @PathVariable UUID userId){
        return ResponseEntity.ok(allergyService.getAllergyById(userId,allergyId));
    }


    @GetMapping("/all")
    public ResponseEntity<List<AllergyResponseDTO>> getAllAllergiesForUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(allergyService.getAllAllergiesByUserId(userId));
    }

    @PutMapping("/update/{allergyId}")
    public ResponseEntity<AllergyResponseDTO> updateAllergy(
            @PathVariable UUID userId,
            @PathVariable UUID allergyId,
            @RequestBody AllergyRequestDTO requestDTO) {
        return ResponseEntity.ok(allergyService.updateAllergy(userId, allergyId, requestDTO));
    }

    @DeleteMapping("/delete/{allergyId}")
    public ResponseEntity<Void> deleteAllergy(
            @PathVariable UUID userId,
            @PathVariable UUID allergyId) {
        allergyService.deleteAllergy(userId, allergyId);
        return ResponseEntity.noContent().build();
    }
}
