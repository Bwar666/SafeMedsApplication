package com.safemeds.safemedsbackend.enums;

public enum AiWarningType {
    ALLERGY_CONFLICT,        // Contains allergens
    INTERACTION_CONFLICT,    // Interacts with another medicine
    OVERDOSE_WARNING,        // Too much dosage
    DUPLICATE_MEDICINE,      // Same medicine added multiple times
    TIMING_CONFLICT,         // Conflicting intake times
    FOOD_INSTRUCTION_ISSUE,  // Incorrect food instruction (e.g., must take with food)
    OTHER                    // General category
}
