package com.safemeds.safemedsbackend.services.search;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private String lastSearchSource = "";

    public List<SearchSuggestionDTO> search(String query, int limit) {
        List<SearchSuggestionDTO> results = new ArrayList<>();

        try {
            log.info("Searching RxTerms for medicines: {}", query);
            List<SearchSuggestionDTO> rxTermsResults = searchRxTerms(query, limit);
            results.addAll(rxTermsResults);

            if (!rxTermsResults.isEmpty()) {
                lastSearchSource = "RxTerms-API";
                log.info("RxTerms returned {} results", rxTermsResults.size());
            } else {
                lastSearchSource = "No results found";
                log.info("RxTerms returned no results for: {}", query);
            }
        } catch (Exception e) {
            log.error("RxTerms API failed: {}", e.getMessage(), e);
            lastSearchSource = "API Error: " + e.getMessage();
        }

        return removeDuplicates(results).stream().limit(limit).collect(Collectors.toList());
    }

    private List<SearchSuggestionDTO> searchRxTerms(String query, int limit) {
        String url = UriComponentsBuilder
                .fromUriString("https://clinicaltables.nlm.nih.gov/api/rxterms/v3/search")
                .queryParam("terms", query)
                .queryParam("maxList", Math.min(limit, 10))
                .queryParam("df", "DISPLAY_NAME") // Display field - user-friendly names
                .toUriString();

        log.info("RxTerms URL: {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("User-Agent", "SafeMeds-Backend/1.0");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            // Get response as String first to log it
            ResponseEntity<String> stringResponse = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            log.info("API Response Status: {}", stringResponse.getStatusCode());
            log.info("Raw API Response: {}", stringResponse.getBody());

            if (stringResponse.getStatusCode() == HttpStatus.OK && stringResponse.getBody() != null) {
                return parseRxTermsResponse(stringResponse.getBody(), query, limit);
            }

        } catch (Exception e) {
            log.error("Error calling RxTerms API: {}", e.getMessage(), e);
        }

        log.warn("RxTerms API returned empty or invalid response");
        return Collections.emptyList();
    }

    private List<SearchSuggestionDTO> parseRxTermsResponse(String responseBody, String query, int limit) {
        List<SearchSuggestionDTO> results = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        try {
            // Parse the JSON response into an array
            Object[] responseArray = objectMapper.readValue(responseBody, Object[].class);

            log.info("Response array length: {}", responseArray.length);

            if (responseArray.length < 2) {
                log.warn("Response array too short, expected at least 2 elements, got: {}", responseArray.length);
                return results;
            }

            // Element 0: Total count
            Integer totalCount = (Integer) responseArray[0];
            log.info("Total results available: {}", totalCount);

            // Element 1: Array of medicine names
            @SuppressWarnings("unchecked")
            List<String> medicineNames = (List<String>) responseArray[1];
            log.info("Number of medicine names returned: {}", medicineNames != null ? medicineNames.size() : 0);

            if (medicineNames != null && !medicineNames.isEmpty()) {
                for (String medicineName : medicineNames) {
                    if (results.size() >= limit) break;

                    if (medicineName != null && !medicineName.trim().isEmpty()) {
                        String cleanName = cleanMedicineName(medicineName);

                        if (cleanName.length() >= 2 && !seen.contains(cleanName.toLowerCase())) {
                            results.add(SearchSuggestionDTO.builder()
                                    .value(cleanName)
                                    .displayName(cleanName)
                                    .description("Medication")
                                    .source("RxTerms")
                                    .category("MEDICINE")
                                    .build());

                            seen.add(cleanName.toLowerCase());
                            log.debug("Added medicine: {}", cleanName);
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error parsing RxTerms response: {}", e.getMessage(), e);
            log.error("Response body was: {}", responseBody);

            // If parsing fails, return empty list instead of crashing
            return Collections.emptyList();
        }

        log.info("Parsed {} medicine results from RxTerms", results.size());
        return results;
    }

    private String cleanMedicineName(String rawName) {
        if (rawName == null) return "";

        String cleaned = rawName.trim();

        // Remove everything in parentheses (Oral Pill), (Chewable), etc.
        cleaned = cleaned.replaceAll("\\s*\\([^)]*\\)\\s*", "");

        // Remove extra spaces and trim
        cleaned = cleaned.replaceAll("\\s+", " ").trim();

        return cleaned;
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