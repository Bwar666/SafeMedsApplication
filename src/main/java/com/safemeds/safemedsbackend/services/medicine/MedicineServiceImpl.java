package com.safemeds.safemedsbackend.services.medicine;

import com.safemeds.safemedsbackend.dtos.medicine.MedicineRequestDTO;
import com.safemeds.safemedsbackend.dtos.medicine.MedicineResponseDTO;
import com.safemeds.safemedsbackend.entities.*;
import com.safemeds.safemedsbackend.mappers.medicine.MedicineMapper;
import com.safemeds.safemedsbackend.repositories.medicine.MedicineRepository;
import com.safemeds.safemedsbackend.repositories.user.UserProfileRepository;
import com.safemeds.safemedsbackend.services.ai.AiWarningService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicineServiceImpl implements MedicineService {

    private final MedicineRepository medicineRepository;
    private final UserProfileRepository userProfileRepository;
    private final MedicineMapper medicineMapper;
    private final IntakeEventService intakeEventService;
    private final AiWarningService aiWarningService;

    @Override
    @Transactional
    public MedicineResponseDTO createMedicine(UUID userId, MedicineRequestDTO dto) {
        UserProfile user = userProfileRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Medicine medicine = medicineMapper.toEntity(dto);
        medicine.setUserProfile(user);
        Medicine savedMedicine = medicineRepository.saveAndFlush(medicine);
        intakeEventService.generateIntakeEvents(savedMedicine);


        aiWarningService.evaluateAndCreateWarnings(userId, savedMedicine);


        return medicineMapper.toResponseDTO(savedMedicine);
    }

    @Override
    public List<MedicineResponseDTO> getAllMedicinesByUser(UUID userId) {
        validateUserExists(userId);
        List<Medicine> medicines = medicineRepository.findByUserProfileId(userId);
        return medicineMapper.toResponseDTOList(medicines);
    }

    @Override
    public MedicineResponseDTO getMedicineById(UUID userId, UUID medicineId) {
        Medicine medicine = getMedicineForUser(medicineId, userId);
        return medicineMapper.toResponseDTO(medicine);
    }

    @Override
    @Transactional
    public MedicineResponseDTO updateMedicine(UUID userId, UUID medicineId, MedicineRequestDTO dto) {
        Medicine existingMedicine = getMedicineForUser(medicineId, userId);

        medicineMapper.updateEntityFromDto(dto, existingMedicine);
        existingMedicine.setUpdatedAt(LocalDateTime.now());
        Medicine updatedMedicine = medicineRepository.saveAndFlush(existingMedicine);
        intakeEventService.updateIntakeEvents(updatedMedicine);

        try {
            aiWarningService.evaluateAndCreateWarnings(userId, updatedMedicine);
            log.info("AI warnings updated for user {} and medicine {}", userId, updatedMedicine.getName());
        } catch (Exception e) {
            log.error("AI warning update failed for user {} and medicine {}: {}",
                    userId, updatedMedicine.getName(), e.getMessage(), e);
        }

        return medicineMapper.toResponseDTO(updatedMedicine);
    }

    @Override
    @Transactional
    public void deleteMedicine(UUID userId, UUID medicineId) {
        Medicine medicine = getMedicineForUser(medicineId, userId);
        intakeEventService.deleteIntakeEventsForMedicine(medicineId);
        medicineRepository.delete(medicine);

        log.info("Medicine {} deleted for user {}", medicine.getName(), userId);
    }

    @Override
    public List<MedicineResponseDTO> getActiveMedicines(UUID userId) {
        validateUserExists(userId);
        List<Medicine> activeMedicines = medicineRepository.findByUserProfileIdAndIsActiveTrue(userId);
        return medicineMapper.toResponseDTOList(activeMedicines);
    }

    @Override
    @Transactional
    public MedicineResponseDTO archiveMedicine(UUID userId, UUID medicineId) {
        Medicine medicine = getMedicineForUser(medicineId, userId);
        medicine.setIsActive(false);
        medicine.setUpdatedAt(LocalDateTime.now());
        Medicine archived = medicineRepository.save(medicine);

        log.info("Medicine {} archived for user {}", medicine.getName(), userId);
        return medicineMapper.toResponseDTO(archived);
    }

    private void validateUserExists(UUID userId) {
        if (!userProfileRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found");
        }
    }

    private Medicine getMedicineForUser(UUID medicineId, UUID userId) {
        Medicine medicine = medicineRepository.findByIdAndUserProfileId(medicineId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Medicine not found"));

        if (!medicine.getUserProfile().getId().equals(userId)) {
            throw new SecurityException("Unauthorized access");
        }

        return medicine;
    }
}