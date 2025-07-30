package com.safemeds.safemedsbackend.services.ai;

import com.safemeds.safemedsbackend.dtos.ai.AiEvaluationInputDTO;
import org.springframework.stereotype.Service;

@Service
public class PromptBuilderServiceImpl implements PromptBuilderService {

    @Override
    public String buildPrompt(AiEvaluationInputDTO input) {
        return switch (input.getRiskType()) {
            case ALLERGY_CONFLICT -> buildAllergyPrompt(input);
            case INTERACTION_CONFLICT -> buildInteractionPrompt(input);
            case OVERDOSE_WARNING -> buildOverdosePrompt(input);
            case DUPLICATE_MEDICINE -> buildDuplicatePrompt(input);
            case TIMING_CONFLICT -> buildTimingPrompt(input);
            case FOOD_INSTRUCTION_ISSUE -> buildFoodPrompt(input);
            case NO_RISK -> buildNoRiskPrompt(input);
        };
    }

    private String buildAllergyPrompt(AiEvaluationInputDTO input) {
        return String.format(
                "Analyze allergy risk for medicine %s in patient allergic to %s. " +
                        "Respond with JSON containing: type as ALLERGY_CONFLICT, severity as LOW/MEDIUM/HIGH/CRITICAL, " +
                        "message explaining the risk, and details object with reason/recommendation/mechanism/alternatives fields.",
                input.getMedicineName(),
                input.getAllergyName()
        );
    }

    private String buildInteractionPrompt(AiEvaluationInputDTO input) {
        String userMedicinesStr = (input.getUserMedicines() != null && !input.getUserMedicines().isEmpty())
                ? String.join(", ", input.getUserMedicines())
                : "none";

        return String.format(
                "Analyze drug interaction between %s and %s. Other patient medicines include %s. " +
                        "Check for dangerous interactions, contraindications or synergistic effects. " +
                        "Respond with JSON containing: type as INTERACTION_CONFLICT, severity as LOW/MEDIUM/HIGH/CRITICAL, " +
                        "message explaining the interaction risk, and details object with reason/recommendation/mechanism/alternatives fields.",
                input.getMedicineName(),
                input.getTargetMedicineName(),
                userMedicinesStr
        );
    }

    private String buildOverdosePrompt(AiEvaluationInputDTO input) {
        return String.format(
                "Analyze overdose risk for medicine %s with dosage %s %s. " +
                        "Check if this dosage could lead to overdose, toxicity or accumulation. " +
                        "Respond with JSON containing: type as OVERDOSE_WARNING, severity as LOW/MEDIUM/HIGH/CRITICAL, " +
                        "message explaining overdose risk, and details object with reason/recommendation/mechanism/alternatives fields.",
                input.getMedicineName(),
                input.getDosageAmount() != null ? input.getDosageAmount().toString() : "standard",
                input.getDosageUnit() != null ? input.getDosageUnit() : "dose"
        );
    }

    private String buildDuplicatePrompt(AiEvaluationInputDTO input) {
        String userMedicinesStr = (input.getUserMedicines() != null && !input.getUserMedicines().isEmpty())
                ? String.join(", ", input.getUserMedicines())
                : "none";

        return String.format(
                "Check if medicine %s duplicates %s. Patient medicines include %s. " +
                        "Determine if these medicines are duplicates or contain same active ingredient. " +
                        "Respond with JSON containing: type as DUPLICATE_MEDICINE, severity as LOW/MEDIUM/HIGH/CRITICAL, " +
                        "message explaining if duplicate, and details object with reason/recommendation/mechanism/alternatives fields.",
                input.getMedicineName(),
                input.getTargetMedicineName(),
                userMedicinesStr
        );
    }

    private String buildTimingPrompt(AiEvaluationInputDTO input) {
        return String.format(
                "Analyze timing conflict between %s and %s. " +
                        "Check if taking these medicines at similar times causes absorption issues or timing-dependent interactions. " +
                        "Respond with JSON containing: type as TIMING_CONFLICT, severity as LOW/MEDIUM/HIGH/CRITICAL, " +
                        "message explaining timing issues, and details object with reason/recommendation/mechanism/alternatives fields.",
                input.getMedicineName(),
                input.getTargetMedicineName() != null ? input.getTargetMedicineName() : "other medicines"
        );
    }

    private String buildFoodPrompt(AiEvaluationInputDTO input) {
        return String.format(
                "Analyze food instructions for medicine %s with instructions %s. " +
                        "Check if food intake instructions affect absorption or bioavailability. " +
                        "Respond with JSON containing: type as FOOD_INSTRUCTION_ISSUE, severity as LOW/MEDIUM/HIGH/CRITICAL, " +
                        "message explaining food interaction, and details object with reason/recommendation/mechanism/alternatives fields.",
                input.getMedicineName(),
                input.getFoodInstructions() != null ? input.getFoodInstructions() : "none specified"
        );
    }

    private String buildNoRiskPrompt(AiEvaluationInputDTO input) {
        return String.format(
                "General safety check for medicine %s. No specific risks found in other categories. " +
                        "Perform final safety evaluation for any overlooked considerations. " +
                        "Respond with JSON containing: type as NO_RISK, severity as LOW, " +
                        "message confirming safety, and details object with reason/recommendation/mechanism/alternatives fields.",
                input.getMedicineName()
        );
    }
}