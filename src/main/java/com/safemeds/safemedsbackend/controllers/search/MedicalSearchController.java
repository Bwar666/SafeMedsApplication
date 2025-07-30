package com.safemeds.safemedsbackend.controllers.search;

import com.safemeds.safemedsbackend.dtos.search.SearchResponseDTO;
import com.safemeds.safemedsbackend.services.search.MedicalSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class MedicalSearchController {

    private final MedicalSearchService medicalSearchService;

    @GetMapping("/medicines")
    public CompletableFuture<ResponseEntity<SearchResponseDTO>> searchMedicines(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {

        if (query == null || query.trim().length() < 2) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.badRequest().build()
            );
        }

        return medicalSearchService.searchMedicines(query.trim(), Math.min(limit, 20))
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/conditions")
    public CompletableFuture<ResponseEntity<SearchResponseDTO>> searchConditions(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {

        if (query == null || query.trim().length() < 2) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.badRequest().build()
            );
        }

        return medicalSearchService.searchConditions(query.trim(), Math.min(limit, 20))
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/allergies")
    public CompletableFuture<ResponseEntity<SearchResponseDTO>> searchAllergies(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {

        if (query == null || query.trim().length() < 2) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.badRequest().build()
            );
        }

        return medicalSearchService.searchAllergies(query.trim(), Math.min(limit, 20))
                .thenApply(ResponseEntity::ok);
    }
}
