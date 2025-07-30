// ConditionSearchService.java - Fixed with proper Clinical Tables API parsing
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
public class ConditionSearchService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private String lastSearchSource = "";

    public List<SearchSuggestionDTO> search(String query, int limit) {
        List<SearchSuggestionDTO> results = new ArrayList<>();

        try {
            log.info("Searching Clinical Tables for conditions: {}", query);
            List<SearchSuggestionDTO> clinicalTablesResults = searchClinicalTables(query, limit);
            results.addAll(clinicalTablesResults);

            if (!clinicalTablesResults.isEmpty()) {
                lastSearchSource = "Clinical-Tables";
                log.info("Clinical Tables returned {} results", clinicalTablesResults.size());
            } else {
                lastSearchSource = "No results found";
                log.info("Clinical Tables returned no results for: {}", query);
            }
        } catch (Exception e) {
            log.error("Clinical Tables API failed: {}", e.getMessage(), e);
            lastSearchSource = "API Error: " + e.getMessage();
        }

        return removeDuplicates(results).stream().limit(limit).collect(Collectors.toList());
    }

    private List<SearchSuggestionDTO> searchClinicalTables(String query, int limit) {
        // Use consumer_name for display as it's more user-friendly
        String url = UriComponentsBuilder
                .fromUriString("https://clinicaltables.nlm.nih.gov/api/conditions/v3/search")
                .queryParam("terms", query)
                .queryParam("count", Math.min(limit, 10))
                .queryParam("df", "consumer_name") // Display field - what we show to user
                .queryParam("ef", "primary_name,icd10cm_codes") // Extra fields for additional data
                .toUriString();

        log.info("Clinical Tables URL: {}", url);

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
                return parseClinicalTablesResponse(stringResponse.getBody(), query, limit);
            }

        } catch (Exception e) {
            log.error("Error calling Clinical Tables API: {}", e.getMessage(), e);
        }

        log.warn("Clinical Tables API returned empty or invalid response");
        return Collections.emptyList();
    }

    private List<SearchSuggestionDTO> parseClinicalTablesResponse(String responseBody, String query, int limit) {
        List<SearchSuggestionDTO> results = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        try {
            // Parse the JSON response into an array
            Object[] responseArray = objectMapper.readValue(responseBody, Object[].class);

            log.info("Response array length: {}", responseArray.length);

            if (responseArray.length < 4) {
                log.warn("Response array too short, expected at least 4 elements, got: {}", responseArray.length);
                return results;
            }

            // Element 0: Total count
            Integer totalCount = (Integer) responseArray[0];
            log.info("Total results available: {}", totalCount);

            // Element 1: Array of codes/IDs
            @SuppressWarnings("unchecked")
            List<String> codes = (List<String>) responseArray[1];
            log.info("Number of codes returned: {}", codes != null ? codes.size() : 0);

            // Element 2: Extra fields data (map)
            @SuppressWarnings("unchecked")
            Map<String, List<String>> extraData = (Map<String, List<String>>) responseArray[2];

            // Element 3: Display fields data (array of arrays)
            @SuppressWarnings("unchecked")
            List<List<String>> displayData = (List<List<String>>) responseArray[3];
            log.info("Number of display items: {}", displayData != null ? displayData.size() : 0);

            if (displayData != null && !displayData.isEmpty()) {
                for (int i = 0; i < displayData.size() && results.size() < limit; i++) {
                    List<String> displayItem = displayData.get(i);

                    if (displayItem != null && !displayItem.isEmpty()) {
                        String consumerName = displayItem.get(0); // consumer_name from df parameter

                        if (consumerName != null && !consumerName.trim().isEmpty()) {
                            String cleanName = cleanConditionName(consumerName);

                            if (cleanName.length() >= 2 && !seen.contains(cleanName.toLowerCase())) {
                                // Get additional data if available
                                String primaryName = null;
                                String icdCodes = null;

                                if (extraData != null) {
                                    if (extraData.containsKey("primary_name") && extraData.get("primary_name").size() > i) {
                                        primaryName = extraData.get("primary_name").get(i);
                                    }
                                    if (extraData.containsKey("icd10cm_codes") && extraData.get("icd10cm_codes").size() > i) {
                                        icdCodes = extraData.get("icd10cm_codes").get(i);
                                    }
                                }

                                String description = "Medical condition";
                                if (primaryName != null && !primaryName.equals(cleanName)) {
                                    description = "Medical condition (" + primaryName + ")";
                                }

                                results.add(SearchSuggestionDTO.builder()
                                        .value(cleanName)
                                        .displayName(cleanName)
                                        .description(description)
                                        .source("Clinical-Tables")
                                        .category("CONDITION")
                                        .build());

                                seen.add(cleanName.toLowerCase());
                                log.debug("Added condition: {}", cleanName);
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error parsing Clinical Tables response: {}", e.getMessage(), e);
            log.error("Response body was: {}", responseBody);
        }

        log.info("Parsed {} condition results from Clinical Tables", results.size());
        return results;
    }

    private String cleanConditionName(String rawName) {
        if (rawName == null) return "";

        // Remove HTML entities and tags
        String cleaned = rawName.replaceAll("&[a-zA-Z]+;", "")
                .replaceAll("<[^>]*>", "")
                .trim();

        // Remove ICD codes and medical codes
        cleaned = cleaned.replaceAll("\\b[A-Z]\\d{1,3}(\\.\\d+)?\\b", "")
                .replaceAll("\\b\\d{3}(\\.\\d+)?\\b", "");

        // Remove extra medical terminology suffixes
        cleaned = cleaned.replaceAll("\\s*,\\s*(unspecified|NOS|not otherwise specified|without complications)\\b", "")
                .replaceAll("\\s*\\(.*?\\)\\s*", " "); // Remove parenthetical info

        // Remove leading/trailing punctuation and normalize whitespace
        cleaned = cleaned.replaceAll("^[,\\-\\s]+|[,\\-\\s]+$", "")
                .replaceAll("\\s+", " ")
                .trim();

        // If it's still too long, try to get the main part
        if (cleaned.length() > 50) {
            String[] parts = cleaned.split("[,;]");
            if (parts.length > 0) {
                cleaned = parts[0].trim();
            }
        }

        return capitalizeConditionName(cleaned);
    }

    private String capitalizeConditionName(String text) {
        if (text == null || text.isEmpty()) return text;

        String[] keepLowercase = {"of", "and", "or", "in", "on", "with", "without", "the", "a", "an"};
        String[] words = text.toLowerCase().split("\\s+");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (!word.isEmpty()) {
                if (result.length() > 0) result.append(" ");

                if (i == 0 || !Arrays.asList(keepLowercase).contains(word)) {
                    result.append(Character.toUpperCase(word.charAt(0)))
                            .append(word.substring(1));
                } else {
                    result.append(word);
                }
            }
        }

        return result.toString();
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