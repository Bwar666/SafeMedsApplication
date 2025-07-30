package com.safemeds.safemedsbackend.services.ai;

import com.safemeds.safemedsbackend.dtos.ai.AiEvaluationInputDTO;
import com.safemeds.safemedsbackend.entities.AiEvaluationResult;

import java.util.List;

public interface AiEvaluationService {
    AiEvaluationResult getOrCreateEvaluation(AiEvaluationInputDTO input);
    List<AiEvaluationResult> evaluateAllRisks(String medicineName,
                                              List<String> userMedicines,
                                              List<String> userAllergies);
}