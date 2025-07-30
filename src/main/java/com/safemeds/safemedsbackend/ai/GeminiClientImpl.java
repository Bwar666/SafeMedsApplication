package com.safemeds.safemedsbackend.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeminiClientImpl implements GeminiClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.timeout:30s}")
    private Duration timeout;

    @Override
    public String generateAiResponse(String prompt) {
        try {
            String cleanPrompt = cleanPrompt(prompt);
            String requestBody = buildRequestBody(cleanPrompt);

            log.debug("Sending clean prompt to Gemini: {}", cleanPrompt);

            String response = webClient.post()
                    .uri(geminiApiUrl + "?key=" + apiKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.isError(), clientResponse -> {
                        return clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("Gemini API error: {}", errorBody);
                                    return Mono.error(new GeminiApiException("Gemini API error: " + errorBody));
                                });
                    })
                    .bodyToMono(String.class)
                    .timeout(timeout)
                    .onErrorResume(Exception.class, error -> {
                        log.error("Gemini API call failed: {}", error.getMessage());
                        return Mono.just(createErrorResponse(error.getMessage()));
                    })
                    .block();

            return extractTextFromGeminiResponse(response);

        } catch (Exception ex) {
            log.error("Gemini API call failed: {}", ex.getMessage(), ex);
            return createErrorResponse(ex.getMessage());
        }
    }

    private String cleanPrompt(String prompt) {
        if (prompt == null) return "";

        return prompt
                .replaceAll("\\r\\n|\\r|\\n", " ")  // Replace newlines with spaces
                .replaceAll("\\s+", " ")             // Replace multiple spaces with single space
                .replaceAll("\"", "'")               // Replace double quotes with single quotes
                .trim();
    }

    private String buildRequestBody(String prompt) {
        try {
            String escapedPrompt = objectMapper.writeValueAsString(prompt);
            escapedPrompt = escapedPrompt.substring(1, escapedPrompt.length() - 1);

            return String.format("""
                {
                  "contents": [
                    {
                      "parts": [
                        { "text": "%s" }
                      ]
                    }
                  ],
                  "generationConfig": {
                    "temperature": 0.1,
                    "topK": 1,
                    "topP": 1,
                    "maxOutputTokens": 1024
                  }
                }
                """, escapedPrompt);
        } catch (Exception e) {
            log.error("Failed to build request body: {}", e.getMessage());
            return String.format("""
                {
                  "contents": [
                    {
                      "parts": [
                        { "text": "Analyze medicine safety and respond with JSON containing type, severity, message, and details fields." }
                      ]
                    }
                  ]
                }
                """);
        }
    }

    private String extractTextFromGeminiResponse(String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);

            if (root.has("error")) {
                log.error("Gemini API returned error: {}", root.path("error"));
                return createErrorResponse("Gemini API error");
            }

            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode firstCandidate = candidates.get(0);
                JsonNode content = firstCandidate.path("content");
                JsonNode parts = content.path("parts");

                if (parts.isArray() && parts.size() > 0) {
                    String text = parts.get(0).path("text").asText();
                    log.debug("Successfully extracted text from Gemini response");
                    return cleanJsonResponse(text);
                }
            }

            return createErrorResponse("Invalid response structure");

        } catch (Exception e) {
            log.error("Failed to parse Gemini response: {}", e.getMessage());
            return createErrorResponse("Response parsing failed");
        }
    }

    private String cleanJsonResponse(String response) {
        int jsonStart = response.indexOf('{');
        int jsonEnd = response.lastIndexOf('}') + 1;

        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            return response.substring(jsonStart, jsonEnd);
        }

        return createErrorResponse("No valid JSON in response");
    }

    private String createErrorResponse(String error) {
        return String.format("""
            {
              "type": "NO_RISK",
              "severity": "LOW",
              "message": "AI evaluation temporarily unavailable",
              "details": {
                "reason": "Technical issue: %s",
                "recommendation": "Consult healthcare provider",
                "mechanism": "System fallback",
                "alternatives": "Manual review recommended"
              }
            }
            """, error.replaceAll("\"", "'"));
    }
}