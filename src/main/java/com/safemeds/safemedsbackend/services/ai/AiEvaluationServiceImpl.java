package com.safemeds.safemedsbackend.services.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safemeds.safemedsbackend.ai.GeminiClient;
import com.safemeds.safemedsbackend.dtos.ai.AiEvaluationInputDTO;
import com.safemeds.safemedsbackend.entities.AiEvaluationResult;
import com.safemeds.safemedsbackend.enums.*;
import com.safemeds.safemedsbackend.repositories.ai.AiEvaluationResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiEvaluationServiceImpl implements AiEvaluationService {

    private final AiEvaluationResultRepository evaluationRepository;
    private final GeminiClient geminiClient;
    private final PromptBuilderService promptBuilderService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public AiEvaluationResult getOrCreateEvaluation(AiEvaluationInputDTO input) {
        Optional<AiEvaluationResult> existing = findExistingSuccessfulEvaluation(input);
        if (existing.isPresent()) {
            log.debug("Found existing successful evaluation for {} - {}", input.getMedicineName(), input.getRiskType());
            return existing.get();
        }

        return createNewEvaluation(input);
    }

    @Override
    @Transactional
    public List<AiEvaluationResult> evaluateAllRisks(String medicineName,
                                                     List<String> userMedicines,
                                                     List<String> userAllergies) {
        List<AiEvaluationResult> results = new ArrayList<>();

        log.info("Starting AI evaluation for medicine: {} with {} existing medicines and {} allergies",
                medicineName, userMedicines != null ? userMedicines.size() : 0,
                userAllergies != null ? userAllergies.size() : 0);

        // 1. Check allergy conflicts
        if (userAllergies != null && !userAllergies.isEmpty()) {
            for (String allergy : userAllergies) {
                AiEvaluationInputDTO input = AiEvaluationInputDTO.builder()
                        .medicineName(medicineName)
                        .riskType(AiWarningType.ALLERGY_CONFLICT)
                        .allergyName(allergy)
                        .userMedicines(userMedicines)
                        .userAllergies(userAllergies)
                        .build();

                AiEvaluationResult result = getOrCreateEvaluation(input);
                if (shouldIncludeResult(result)) {
                    results.add(result);
                }
            }
        }

        // 2. Check drug interactions
        if (userMedicines != null && !userMedicines.isEmpty()) {
            for (String existingMedicine : userMedicines) {
                if (!existingMedicine.equalsIgnoreCase(medicineName)) {
                    AiEvaluationInputDTO input = AiEvaluationInputDTO.builder()
                            .medicineName(medicineName)
                            .riskType(AiWarningType.INTERACTION_CONFLICT)
                            .targetMedicineName(existingMedicine)
                            .userMedicines(userMedicines)
                            .userAllergies(userAllergies)
                            .build();

                    AiEvaluationResult result = getOrCreateEvaluation(input);
                    if (shouldIncludeResult(result)) {
                        results.add(result);
                    }
                }
            }
        }

        // 3. Check duplicate medicines
        if (userMedicines != null && !userMedicines.isEmpty()) {
            for (String existingMedicine : userMedicines) {
                if (!existingMedicine.equalsIgnoreCase(medicineName)) {
                    AiEvaluationInputDTO input = AiEvaluationInputDTO.builder()
                            .medicineName(medicineName)
                            .riskType(AiWarningType.DUPLICATE_MEDICINE)
                            .targetMedicineName(existingMedicine)
                            .userMedicines(userMedicines)
                            .build();

                    AiEvaluationResult result = getOrCreateEvaluation(input);
                    if (shouldIncludeResult(result)) {
                        results.add(result);
                    }
                }
            }
        }

        // 4. Check individual risk types (overdose, timing, food)
        for (AiWarningType riskType : List.of(
                AiWarningType.OVERDOSE_WARNING,
                AiWarningType.TIMING_CONFLICT,
                AiWarningType.FOOD_INSTRUCTION_ISSUE)) {

            AiEvaluationInputDTO input = AiEvaluationInputDTO.builder()
                    .medicineName(medicineName)
                    .riskType(riskType)
                    .userMedicines(userMedicines)
                    .userAllergies(userAllergies)
                    .build();

            AiEvaluationResult result = getOrCreateEvaluation(input);
            if (shouldIncludeResult(result)) {
                results.add(result);
            }
        }

        // 5. Add NO_RISK evaluation only if no significant risks found
        boolean hasSignificantRisk = results.stream()
                .anyMatch(r -> r.getSeverity() == WarningSeverity.HIGH || r.getSeverity() == WarningSeverity.CRITICAL);

        if (!hasSignificantRisk) {
            AiEvaluationInputDTO input = AiEvaluationInputDTO.builder()
                    .medicineName(medicineName)
                    .riskType(AiWarningType.NO_RISK)
                    .userMedicines(userMedicines)
                    .userAllergies(userAllergies)
                    .build();

            AiEvaluationResult result = getOrCreateEvaluation(input);
            if (shouldIncludeResult(result)) {
                results.add(result);
            }
        }

        log.info("Completed AI evaluation for medicine: {} - generated {} valid evaluations",
                medicineName, results.size());

        return results;
    }

    private Optional<AiEvaluationResult> findExistingSuccessfulEvaluation(AiEvaluationInputDTO input) {
        String medicineName = input.getMedicineName().trim().toLowerCase();
        AiWarningType riskType = input.getRiskType();

        Optional<AiEvaluationResult> result = switch (riskType) {
            case ALLERGY_CONFLICT -> {
                if (input.getAllergyName() == null) {
                    log.warn("Allergy name is null for ALLERGY_CONFLICT evaluation");
                    yield Optional.empty();
                }
                String allergyName = input.getAllergyName().trim().toLowerCase();
                yield evaluationRepository.findByMedicineNameIgnoreCaseAndRiskTypeAndAllergyNameIgnoreCase(
                        medicineName, riskType, allergyName);
            }
            case INTERACTION_CONFLICT, DUPLICATE_MEDICINE, TIMING_CONFLICT -> {
                if (input.getTargetMedicineName() == null) {
                    log.warn("Target medicine name is null for {} evaluation", riskType);
                    yield Optional.empty();
                }
                String targetMedicine = input.getTargetMedicineName().trim().toLowerCase();
                yield evaluationRepository.findByMedicineNameIgnoreCaseAndRiskTypeAndTargetMedicineNameIgnoreCase(
                        medicineName, riskType, targetMedicine);
            }
            case OVERDOSE_WARNING, FOOD_INSTRUCTION_ISSUE, NO_RISK ->
                    evaluationRepository.findByMedicineNameIgnoreCaseAndRiskTypeAndAllergyNameIsNullAndTargetMedicineNameIsNull(
                            medicineName, riskType);
        };

        return result.filter(eval -> eval.getStatus() == AiWarningStatus.COMPLETED && eval.getSource() == AiWarningSource.AI);
    }

    private AiEvaluationResult createNewEvaluation(AiEvaluationInputDTO input) {
        try {
            log.debug("Creating new AI evaluation for {} - {}", input.getMedicineName(), input.getRiskType());

            String prompt = promptBuilderService.buildPrompt(input);
            String aiResponse = geminiClient.generateAiResponse(prompt);

            if (isErrorResponse(aiResponse)) {
                log.warn("Received error response from AI for {} - {}: {}",
                        input.getMedicineName(), input.getRiskType(), aiResponse);


                return createTemporaryFallbackResult(input);
            }


            JsonNode responseJson = objectMapper.readTree(aiResponse);

            if (!isValidAiResponse(responseJson)) {
                log.warn("Invalid AI response structure for {} - {}", input.getMedicineName(), input.getRiskType());
                return createTemporaryFallbackResult(input);
            }

            AiEvaluationResult result = AiEvaluationResult.builder()
                    .medicineName(input.getMedicineName().trim().toLowerCase())
                    .riskType(input.getRiskType())
                    .allergyName(input.getAllergyName() != null ? input.getAllergyName().trim().toLowerCase() : null)
                    .targetMedicineName(input.getTargetMedicineName() != null ? input.getTargetMedicineName().trim().toLowerCase() : null)
                    .message(responseJson.path("message").asText())
                    .severity(WarningSeverity.valueOf(responseJson.path("severity").asText().toUpperCase()))
                    .status(AiWarningStatus.COMPLETED)
                    .source(AiWarningSource.AI)
                    .details(objectMapper.writeValueAsString(responseJson.path("details")))
                    .build();


            AiEvaluationResult savedResult = evaluationRepository.save(result);
            log.debug("Successfully created and saved AI evaluation with ID: {}", savedResult.getId());
            return savedResult;

        } catch (Exception e) {
            log.error("Failed to create AI evaluation for {} - {}: {}",
                    input.getMedicineName(), input.getRiskType(), e.getMessage(), e);

            return createTemporaryFallbackResult(input);
        }
    }


    private AiEvaluationResult createTemporaryFallbackResult(AiEvaluationInputDTO input) {
        log.warn("Creating temporary fallback evaluation for {} - {} (NOT saved to database)",
                input.getMedicineName(), input.getRiskType());

        return AiEvaluationResult.builder()
                .medicineName(input.getMedicineName().trim().toLowerCase())
                .riskType(input.getRiskType())
                .allergyName(input.getAllergyName() != null ? input.getAllergyName().trim().toLowerCase() : null)
                .targetMedicineName(input.getTargetMedicineName() != null ? input.getTargetMedicineName().trim().toLowerCase() : null)
                .message("AI evaluation temporarily unavailable. Please consult your healthcare provider.")
                .severity(WarningSeverity.LOW)
                .status(AiWarningStatus.FAILED)
                .source(AiWarningSource.FALLBACK)
                .details("{\"reason\":\"AI service unavailable\",\"recommendation\":\"Consult healthcare provider\",\"mechanism\":\"System fallback\",\"alternatives\":\"Manual consultation recommended\"}")
                .build();
    }

    private boolean isErrorResponse(String response) {
        return response.contains("\"error\"") ||
                response.contains("AI evaluation temporarily unavailable") ||
                response.contains("Technical issue");
    }

    private boolean isValidAiResponse(JsonNode responseJson) {
        return responseJson.has("type") &&
                responseJson.has("severity") &&
                responseJson.has("message") &&
                responseJson.has("details");
    }

    private boolean shouldIncludeResult(AiEvaluationResult result) {
        return result.getStatus() == AiWarningStatus.COMPLETED &&
                result.getSource() == AiWarningSource.AI;
    }
}