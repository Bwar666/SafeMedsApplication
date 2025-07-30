package com.safemeds.safemedsbackend.services.search;

import com.safemeds.safemedsbackend.dtos.search.SearchSuggestionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AllergySearchService {

    private String lastSearchSource = "";


    private final List<String> allergyNames = Arrays.asList(
            // Severe Allergic Reactions
            "Anaphylaxis", "Anaphylactic Shock", "Severe Allergic Reaction", "Life-threatening Allergy",

            // Skin Allergic Reactions
            "Contact Dermatitis", "Allergic Contact Dermatitis", "Eczema", "Atopic Dermatitis",
            "Urticaria", "Hives", "Angioedema", "Skin Rash", "Allergic Rash", "Drug Rash",
            "Stevens-Johnson Syndrome", "Toxic Epidermal Necrolysis", "Erythema Multiforme",
            "Allergic Skin Reaction", "Itchy Skin", "Skin Swelling", "Facial Swelling",

            // Respiratory Allergic Reactions
            "Allergic Asthma", "Bronchospasm", "Wheezing", "Shortness of Breath",
            "Allergic Rhinitis", "Hay Fever", "Seasonal Allergies", "Nasal Congestion",
            "Runny Nose", "Sneezing", "Allergic Cough", "Throat Swelling", "Laryngeal Edema",

            // Eye Allergic Reactions
            "Allergic Conjunctivitis", "Red Eyes", "Itchy Eyes", "Watery Eyes", "Eye Swelling",
            "Eyelid Swelling", "Eye Irritation",

            // Gastrointestinal Allergic Reactions
            "Food Allergy", "Allergic Gastroenteritis", "Nausea", "Vomiting", "Diarrhea",
            "Abdominal Pain", "Food Intolerance", "Oral Allergy Syndrome",

            // Drug Allergic Reactions
            "Drug Allergy", "Medication Allergy", "Antibiotic Allergy", "Penicillin Allergy",
            "Sulfa Allergy", "NSAID Allergy", "Aspirin Allergy", "Contrast Allergy",
            "Iodine Allergy", "Latex Allergy", "Anesthetic Allergy",

            // Systemic Allergic Reactions
            "Allergic Reaction", "Hypersensitivity Reaction", "Immune Reaction",
            "Delayed Hypersensitivity", "Immediate Hypersensitivity", "Type I Hypersensitivity",
            "Type II Hypersensitivity", "Type III Hypersensitivity", "Type IV Hypersensitivity",

            // Specific Medical Allergy Terms
            "Serum Sickness", "Drug-Induced Lupus", "Autoimmune Reaction",
            "Cross-Reactivity", "Multiple Drug Allergy Syndrome", "Drug Fever",
            "Eosinophilia", "Drug-Induced Eosinophilia", "DRESS Syndrome",
            "Drug Reaction with Eosinophilia and Systemic Symptoms",

            // Common Allergy Symptoms
            "Itching", "Pruritus", "Skin Burning", "Tingling", "Numbness",
            "Flushing", "Red Skin", "Blistering", "Peeling Skin", "Dry Skin",
            "Cracked Skin", "Bleeding", "Secondary Infection",

            // Respiratory Symptoms
            "Difficulty Breathing", "Chest Tightness", "Hoarseness", "Voice Changes",
            "Sore Throat", "Throat Closing", "Stridor", "Respiratory Distress",

            // Cardiovascular Symptoms
            "Low Blood Pressure", "Hypotension", "Rapid Heart Rate", "Tachycardia",
            "Irregular Heartbeat", "Chest Pain", "Fainting", "Dizziness", "Lightheadedness",

            // Neurological Symptoms
            "Headache", "Confusion", "Anxiety", "Feeling of Doom", "Restlessness",
            "Tremor", "Seizure", "Loss of Consciousness",

            // Environmental Allergies (Medicine-Related)
            "Hospital Environment Allergy", "Medical Equipment Allergy", "Surgical Glove Allergy",
            "Adhesive Allergy", "Bandage Allergy", "Medical Tape Allergy", "Antiseptic Allergy",
            "Cleaning Product Allergy", "Disinfectant Allergy", "Hand Sanitizer Allergy",

            // Injection Site Reactions
            "Injection Site Reaction", "Local Reaction", "Swelling at Injection Site",
            "Pain at Injection Site", "Redness at Injection Site", "Warmth at Injection Site",
            "Nodule Formation", "Abscess Formation", "Cellulitis",

            // Vaccine-Related Allergic Reactions
            "Vaccine Allergy", "Vaccination Reaction", "Vaccine-Induced Allergy",
            "Adjuvant Allergy", "Preservative Allergy", "Stabilizer Allergy",

            // Blood Product Allergic Reactions
            "Blood Transfusion Reaction", "Plasma Allergy", "Platelet Allergy",
            "Red Blood Cell Allergy", "Albumin Allergy", "Immunoglobulin Allergy",

            // Anesthesia-Related Allergic Reactions
            "Anesthesia Allergy", "Malignant Hyperthermia", "Neuromuscular Blocking Agent Allergy",
            "General Anesthesia Allergy", "Local Anesthesia Allergy", "Spinal Anesthesia Allergy",

            // Imaging-Related Allergic Reactions
            "Contrast-Induced Nephropathy", "Contrast Extravasation", "Gadolinium Allergy",
            "Barium Allergy", "CT Contrast Allergy", "MRI Contrast Allergy",

            // Mild to Moderate Reactions
            "Mild Allergic Reaction", "Moderate Allergic Reaction", "Local Allergic Reaction",
            "Systemic Allergic Reaction", "Delayed Allergic Reaction", "Immediate Allergic Reaction"
    );

    public List<SearchSuggestionDTO> search(String query, int limit) {
        List<SearchSuggestionDTO> results = new ArrayList<>();
        String queryLower = query.toLowerCase();


        for (String allergyName : allergyNames) {
            if (allergyName.toLowerCase().contains(queryLower) && results.size() < limit) {
                results.add(SearchSuggestionDTO.builder()
                        .value(allergyName)
                        .displayName(allergyName)
                        .description("Allergic reaction")
                        .source("DATABASE")
                        .category("ALLERGY")
                        .build());
            }
        }


        results.sort((a, b) -> {
            String aValue = a.getValue().toLowerCase();
            String bValue = b.getValue().toLowerCase();

            boolean aExact = aValue.equals(queryLower);
            boolean bExact = bValue.equals(queryLower);
            if (aExact && !bExact) return -1;
            if (!aExact && bExact) return 1;

            boolean aStarts = aValue.startsWith(queryLower);
            boolean bStarts = bValue.startsWith(queryLower);
            if (aStarts && !bStarts) return -1;
            if (!aStarts && bStarts) return 1;

            return a.getValue().compareToIgnoreCase(b.getValue());
        });

        lastSearchSource = results.isEmpty() ? "No allergies found" : "Allergy-Reactions-Database";
        return results.stream().limit(limit).collect(Collectors.toList());
    }

    public String getLastSearchSource() {
        return lastSearchSource;
    }
}