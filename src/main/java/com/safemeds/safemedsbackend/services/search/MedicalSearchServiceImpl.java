package com.safemeds.safemedsbackend.services.search;

import com.safemeds.safemedsbackend.dtos.search.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicalSearchServiceImpl implements MedicalSearchService {

    private final MedicineSearchService medicineSearchService;
    private final ConditionSearchService conditionSearchService;
    private final AllergySearchService allergySearchService;

    @Override
    @Async
    @Cacheable(value = "medicine-search", key = "#query + '-' + #limit")
    public CompletableFuture<SearchResponseDTO> searchMedicines(String query, int limit) {
        log.info("Searching medicines for: {}", query);

        List<SearchSuggestionDTO> results = medicineSearchService.search(query, limit);
        String source = medicineSearchService.getLastSearchSource();

        return CompletableFuture.completedFuture(SearchResponseDTO.builder()
                .suggestions(results)
                .query(query)
                .category("MEDICINE")
                .hasMore(results.size() >= limit)
                .source(source)
                .build());
    }

    @Override
    @Async
    @Cacheable(value = "condition-search", key = "#query + '-' + #limit")
    public CompletableFuture<SearchResponseDTO> searchConditions(String query, int limit) {
        log.info("Searching conditions for: {}", query);

        List<SearchSuggestionDTO> results = conditionSearchService.search(query, limit);
        String source = conditionSearchService.getLastSearchSource();

        return CompletableFuture.completedFuture(SearchResponseDTO.builder()
                .suggestions(results)
                .query(query)
                .category("CONDITION")
                .hasMore(results.size() >= limit)
                .source(source)
                .build());
    }

    @Override
    @Async
    @Cacheable(value = "allergy-search", key = "#query + '-' + #limit")
    public CompletableFuture<SearchResponseDTO> searchAllergies(String query, int limit) {
        log.info("Searching allergies for: {}", query);

        List<SearchSuggestionDTO> results = allergySearchService.search(query, limit);
        String source = allergySearchService.getLastSearchSource();

        return CompletableFuture.completedFuture(SearchResponseDTO.builder()
                .suggestions(results)
                .query(query)
                .category("ALLERGY")
                .hasMore(results.size() >= limit)
                .source(source)
                .build());
    }
}