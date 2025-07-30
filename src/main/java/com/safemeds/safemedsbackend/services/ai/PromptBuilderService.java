package com.safemeds.safemedsbackend.services.ai;

import com.safemeds.safemedsbackend.dtos.ai.AiEvaluationInputDTO;

public interface PromptBuilderService {
    String buildPrompt(AiEvaluationInputDTO input);
}
