package com.safemeds.safemedsbackend.services.search;

import com.safemeds.safemedsbackend.config.MedicalApiConfig;
import com.safemeds.safemedsbackend.dtos.search.SearchSuggestionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicineSearchService {

    private final MedicalApiConfig config;
    private final RestTemplate restTemplate;
    private String lastSearchSource = "";

    public List<SearchSuggestionDTO> search(String query, int limit) {
        List<SearchSuggestionDTO> results = new ArrayList<>();
        List<String> sources = new ArrayList<>();

        try {
            List<SearchSuggestionDTO> rxNormResults = searchRxNorm(query, limit);
            results.addAll(rxNormResults);
            if (!rxNormResults.isEmpty()) {
                sources.add("RxNorm");
            }
        } catch (Exception e) {
            log.warn("RxNorm failed: {}", e.getMessage());
        }

        if (results.size() < limit) {
            try {
                List<SearchSuggestionDTO> fdaResults = searchOpenFDA(query, limit - results.size());
                results.addAll(fdaResults);
                if (!fdaResults.isEmpty()) {
                    sources.add("OpenFDA");
                }
            } catch (Exception e) {
                log.warn("OpenFDA failed: {}", e.getMessage());
            }
        }

        lastSearchSource = sources.isEmpty() ? "No APIs available" : String.join(" + ", sources);
        return removeDuplicates(results).stream().limit(limit).collect(Collectors.toList());
    }

    private List<SearchSuggestionDTO> searchRxNorm(String query, int limit) {
        String url = UriComponentsBuilder
                .fromUriString(config.getRxNorm().getBaseUrl())
                .path("/drugs.json")
                .queryParam("name", query)
                .toUriString();

        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return parseRxNormResponse(response.getBody(), query, limit);
        }
        return Collections.emptyList();
    }

    private List<SearchSuggestionDTO> searchOpenFDA(String query, int limit) {
        String url = UriComponentsBuilder
                .fromUriString(config.getOpenFda().getBaseUrl())
                .path("/drug/label.json")
                .queryParam("api_key", config.getOpenFda().getApiKey())
                .queryParam("search", "openfda.brand_name:" + query + "*")
                .queryParam("limit", Math.min(limit, 10))
                .toUriString();

        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return parseOpenFDAResponse(response.getBody(), limit);
        }
        return Collections.emptyList();
    }

    private List<SearchSuggestionDTO> parseRxNormResponse(Map<String, Object> body, String query, int limit) {
        List<SearchSuggestionDTO> results = new ArrayList<>();

        try {
            Map<String, Object> drugGroup = (Map<String, Object>) body.get("drugGroup");
            if (drugGroup == null) return results;

            List<Map<String, Object>> conceptGroups = (List<Map<String, Object>>) drugGroup.get("conceptGroup");
            if (conceptGroups == null) return results;

            for (Map<String, Object> group : conceptGroups) {
                List<Map<String, Object>> concepts = (List<Map<String, Object>>) group.get("conceptProperties");
                if (concepts != null) {
                    for (Map<String, Object> concept : concepts) {
                        String name = (String) concept.get("name");
                        String rxcui = (String) concept.get("rxcui");

                        if (name != null && name.toLowerCase().contains(query.toLowerCase())) {
                            results.add(SearchSuggestionDTO.builder()
                                    .value(name)
                                    .displayName(name)
                                    .description("Medication from RxNorm")
                                    .source("API")
                                    .category("MEDICINE")
                                    .code(rxcui)
                                    .build());

                            if (results.size() >= limit) return results;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error parsing RxNorm response: {}", e.getMessage());
        }

        return results;
    }

    private List<SearchSuggestionDTO> parseOpenFDAResponse(Map<String, Object> body, int limit) {
        List<SearchSuggestionDTO> results = new ArrayList<>();

        try {
            List<Map<String, Object>> fdaResults = (List<Map<String, Object>>) body.get("results");
            if (fdaResults == null) return results;

            for (Map<String, Object> result : fdaResults) {
                Map<String, Object> openfda = (Map<String, Object>) result.get("openfda");
                if (openfda != null) {
                    List<String> brandNames = (List<String>) openfda.get("brand_name");
                    if (brandNames != null && !brandNames.isEmpty()) {
                        String brandName = brandNames.get(0);
                        results.add(SearchSuggestionDTO.builder()
                                .value(brandName)
                                .displayName(brandName)
                                .description("Brand medication from FDA")
                                .source("API")
                                .category("MEDICINE")
                                .build());

                        if (results.size() >= limit) return results;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error parsing OpenFDA response: {}", e.getMessage());
        }

        return results;
    }

    private List<SearchSuggestionDTO> removeDuplicates(List<SearchSuggestionDTO> suggestions) {
        Set<String> seen = new HashSet<>();
        return suggestions.stream()
                .filter(s -> seen.add(s.getValue().toLowerCase()))
                .collect(Collectors.toList());
    }

    public String getLastSearchSource() {
        return lastSearchSource;
    }
}