package com.safemeds.safemedsbackend.services.ai;

import com.safemeds.safemedsbackend.dtos.ai.AiWarningResponseDTO;
import com.safemeds.safemedsbackend.entities.*;
import com.safemeds.safemedsbackend.enums.AiWarningType;
import com.safemeds.safemedsbackend.enums.WarningSeverity;
import com.safemeds.safemedsbackend.mappers.ai.AiWarningMapper;
import com.safemeds.safemedsbackend.repositories.ai.*;
import com.safemeds.safemedsbackend.repositories.medicine.MedicineRepository;
import com.safemeds.safemedsbackend.repositories.user.UserProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiWarningServiceImpl implements AiWarningService {

    private final AiWarningRepository warningRepository;
    private final UserProfileRepository userProfileRepository;
    private final MedicineRepository medicineRepository;
    private final AiEvaluationService evaluationService;
    private final AiWarningMapper warningMapper;

    @Override
    @Transactional
    public List<AiWarningResponseDTO> evaluateAndCreateWarnings(UUID userId, Medicine medicine) {
        UserProfile user = userProfileRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));


        List<String> userMedicines = getUserMedicineNames(userId);
        List<String> userAllergies = user.getAllergies().stream()
                .map(Allergy::getName)
                .collect(Collectors.toList());


        List<AiEvaluationResult> evaluations = evaluationService.evaluateAllRisks(
                medicine.getName(), userMedicines, userAllergies);


        List<AiWarning> warnings = evaluations.stream()
                .filter(eval -> shouldCreateWarning(eval))
                .map(eval -> createUserWarning(user, medicine, eval))
                .collect(Collectors.toList());

        List<AiWarning> savedWarnings = warningRepository.saveAll(warnings);

        return savedWarnings.stream()
                .map(warningMapper::toDto)
                .collect(Collectors.toList());
    }

    private List<String> getUserMedicineNames(UUID userId) {
        return medicineRepository.findByUserProfileId(userId).stream()
                .map(Medicine::getName)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiWarningResponseDTO> getUserWarnings(UUID userId) {
        return warningRepository.findAllByUserProfileIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(warningMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiWarningResponseDTO> getUnseenWarnings(UUID userId) {
        return warningRepository.findUnseenWarningsByUser(userId)
                .stream()
                .map(warningMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsSeen(UUID warningId, UUID userId) {
        AiWarning warning = getWarningForUser(warningId, userId);
        warning.setSeen(true);
        warningRepository.save(warning);
    }

    @Override
    @Transactional
    public void markAsResolved(UUID warningId, UUID userId) {
        AiWarning warning = getWarningForUser(warningId, userId);
        warning.markAsResolved();
        warningRepository.save(warning);
    }

    @Override
    @Transactional
    public void deleteWarning(UUID warningId, UUID userId) {
        AiWarning warning = getWarningForUser(warningId, userId);
        warningRepository.delete(warning);
        log.info("Warning {} deleted by user {}", warningId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiWarningResponseDTO> getUrgentWarnings(UUID userId) {
        return warningRepository.findAllByUserProfileIdOrderByCreatedAtDesc(userId)
                .stream()
                .filter(warning -> isUrgentWarning(warning))
                .map(warningMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiWarningResponseDTO> getWarningsForMedicine(UUID userId, UUID medicineId) {
        return warningRepository.findAllByUserProfileIdOrderByCreatedAtDesc(userId)
                .stream()
                .filter(warning -> warning.getMedicine().getId().equals(medicineId))
                .map(warningMapper::toDto)
                .collect(Collectors.toList());
    }


    private boolean shouldCreateWarning(AiEvaluationResult evaluation) {
        return evaluation.getRiskType() != AiWarningType.NO_RISK;
    }

    private AiWarning createUserWarning(UserProfile user, Medicine medicine, AiEvaluationResult evaluation) {

        return warningRepository.findByUserProfileIdAndMedicineIdAndEvaluationResultId(
                        user.getId(), medicine.getId(), evaluation.getId())
                .orElseGet(() -> {
                    AiWarning.AiWarningBuilder builder = AiWarning.builder()
                            .userProfile(user)
                            .medicine(medicine)
                            .evaluationResult(evaluation)
                            .seen(false)
                            .resolved(false);


                    if (evaluation.getRiskType() == AiWarningType.ALLERGY_CONFLICT && evaluation.getAllergyName() != null) {
                        user.getAllergies().stream()
                                .filter(allergy -> allergy.getName().equalsIgnoreCase(evaluation.getAllergyName()))
                                .findFirst()
                                .ifPresent(builder::allergy);
                    }

                    return builder.build();
                });
    }

    private AiWarning getWarningForUser(UUID warningId, UUID userId) {
        AiWarning warning = warningRepository.findById(warningId)
                .orElseThrow(() -> new EntityNotFoundException("Warning not found"));

        if (!warning.getUserProfile().getId().equals(userId)) {
            throw new IllegalArgumentException("Warning does not belong to user");
        }

        return warning;
    }

    private boolean isUrgentWarning(AiWarning warning) {
        WarningSeverity severity = warning.getEvaluationResult().getSeverity();
        return severity == WarningSeverity.HIGH || severity == WarningSeverity.CRITICAL;
    }
}