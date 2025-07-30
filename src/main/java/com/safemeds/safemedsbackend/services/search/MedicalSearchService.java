package com.safemeds.safemedsbackend.services.search;

import com.safemeds.safemedsbackend.dtos.search.SearchResponseDTO;
import java.util.concurrent.CompletableFuture;

public interface MedicalSearchService {
    CompletableFuture<SearchResponseDTO> searchMedicines(String query, int limit);
    CompletableFuture<SearchResponseDTO> searchConditions(String query, int limit);
    CompletableFuture<SearchResponseDTO> searchAllergies(String query, int limit);
}