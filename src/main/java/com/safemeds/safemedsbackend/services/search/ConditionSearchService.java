package com.safemeds.safemedsbackend.services.search;

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
    private String lastSearchSource = "";

    public List<SearchSuggestionDTO> search(String query, int limit) {
        List<SearchSuggestionDTO> results = new ArrayList<>();
        List<String> sources = new ArrayList<>();

        try {
            List<SearchSuggestionDTO> clinicalTablesResults = searchClinicalTables(query, limit);
            results.addAll(clinicalTablesResults);
            if (!clinicalTablesResults.isEmpty()) {
                sources.add("Clinical-Tables");
                log.info("Clinical Tables returned {} results", clinicalTablesResults.size());
            }
        } catch (Exception e) {
            log.warn("Clinical Tables failed: {}", e.getMessage());
        }

        if (results.size() < limit) {
            try {
                List<SearchSuggestionDTO> medlineResults = searchMedlinePlus(query, limit - results.size());
                results.addAll(medlineResults);
                if (!medlineResults.isEmpty()) {
                    sources.add("MedlinePlus");
                    log.info("MedlinePlus returned {} results", medlineResults.size());
                }
            } catch (Exception e) {
                log.warn("MedlinePlus failed: {}", e.getMessage());
            }
        }

        lastSearchSource = sources.isEmpty() ? "No APIs available" : String.join(" + ", sources);
        return removeDuplicates(results).stream().limit(limit).collect(Collectors.toList());
    }

    private List<SearchSuggestionDTO> searchClinicalTables(String query, int limit) {
        String url = UriComponentsBuilder
                .fromUriString("https://clinicaltables.nlm.nih.gov/api/conditions/v3/search")
                .queryParam("terms", query)
                .queryParam("maxList", Math.min(limit, 20))
                .queryParam("df", "primary_name,consumer_name,info_link_data")
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("User-Agent", "SafeMeds-Backend/1.0");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return parseClinicalTablesResponse(response.getBody(), query, limit);
        }
        return Collections.emptyList();
    }

    private List<SearchSuggestionDTO> searchMedlinePlus(String query, int limit) {
        String url = UriComponentsBuilder
                .fromUriString("https://wsearch.nlm.nih.gov/ws/query")
                .queryParam("db", "healthTopics")
                .queryParam("term", query)
                .queryParam("retmax", Math.min(limit, 10))
                .queryParam("rettype", "brief")
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
        headers.add("User-Agent", "SafeMeds-Backend/1.0");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return parseMedlinePlusResponse(response.getBody(), query, limit);
        }
        return Collections.emptyList();
    }

    private List<SearchSuggestionDTO> parseClinicalTablesResponse(List responseBody, String query, int limit) {
        List<SearchSuggestionDTO> results = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        try {

            if (responseBody.size() >= 2) {
                List<String> terms = (List<String>) responseBody.get(1);
                for (int i = 0; i < terms.size() && results.size() < limit; i++) {
                    String term = terms.get(i);
                    if (term != null && !seen.contains(term.toLowerCase()) && term.length() > 1) {
                        String cleanName = cleanConditionName(term);

                        if (cleanName.length() < 2 || cleanName.matches("^\\d+$")) {
                            continue;
                        }

                        results.add(SearchSuggestionDTO.builder()
                                .value(cleanName)
                                .displayName(cleanName)
                                .description("Medical condition")
                                .source("API")
                                .category("CONDITION")
                                .build());

                        seen.add(cleanName.toLowerCase());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error parsing Clinical Tables response: {}", e.getMessage());
        }

        return results;
    }

    private List<SearchSuggestionDTO> parseMedlinePlusResponse(String xmlResponse, String query, int limit) {
        List<SearchSuggestionDTO> results = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        try {
            String[] lines = xmlResponse.split("\n");

            for (String line : lines) {
                if (line.contains("<content") && line.contains("title=")) {
                    int start = line.indexOf("title=\"") + 7;
                    int end = line.indexOf("\"", start);

                    if (start > 6 && end > start) {
                        String title = line.substring(start, end)
                                .replaceAll("<[^>]*>", "")
                                .trim();

                        if (title.toLowerCase().contains(query.toLowerCase()) &&
                                !seen.contains(title.toLowerCase()) &&
                                title.length() > 2) {


                            String cleanName = cleanConditionName(title);

                            if (cleanName.length() < 2 || cleanName.matches("^\\d+$")) {
                                continue;
                            }

                            results.add(SearchSuggestionDTO.builder()
                                    .value(cleanName)
                                    .displayName(cleanName)
                                    .description("Health condition")
                                    .source("API")
                                    .category("CONDITION")
                                    .build());

                            seen.add(cleanName.toLowerCase());
                            if (results.size() >= limit) break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error parsing MedlinePlus response: {}", e.getMessage());
        }

        return results;
    }

    private String cleanConditionName(String rawName) {
        if (rawName == null) return "";

        // Remove HTML entities and tags
        String cleaned = rawName.replaceAll("&[a-zA-Z]+;", "")
                .replaceAll("<[^>]*>", "")
                .trim();

        // Remove ICD codes and medical codes (like "E11.9", "250.00")
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

        // Handle special medical terms that should stay lowercase
        String[] keepLowercase = {"of", "and", "or", "in", "on", "with", "without", "the", "a", "an"};

        String[] words = text.toLowerCase().split("\\s+");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (!word.isEmpty()) {
                if (result.length() > 0) result.append(" ");

                // First word is always capitalized, others check the list
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