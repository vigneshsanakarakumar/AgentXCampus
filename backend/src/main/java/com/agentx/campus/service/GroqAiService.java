package com.agentx.campus.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@Service
public class GroqAiService {

    private static final Logger log = LoggerFactory.getLogger(GroqAiService.class);

    private final String apiKey;
    private final String model;
    private final String apiUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GroqAiService(
            @Value("${groq.api.key}") String apiKey,
            @Value("${groq.model:openai/gpt-oss-120b}") String model,
            @Value("${groq.api.url:https://api.groq.com/openai/v1/chat/completions}") String apiUrl,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.model = model;
        this.apiUrl = apiUrl;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String generateResponse(String systemPrompt, String userMessage) {
        try {
            List<Map<String, String>> messages = new ArrayList<>();
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                messages.add(Map.of("role", "system", "content", systemPrompt));
            }
            messages.add(Map.of("role", "user", "content", userMessage));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", this.model);
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.2);
            requestBody.put("max_tokens", 450);

            String jsonPayload = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "AgentX-Campus/1.0")
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode choices = root.path("choices");
                if (choices.isArray() && choices.size() > 0) {
                    return choices.get(0).path("message").path("content").asText().trim();
                }
            } else {
                log.warn("Groq API returned HTTP {}: {}", response.statusCode(), response.body());
            }
        } catch (Exception ex) {
            log.error("Failed to query Groq AI API: {}", ex.getMessage());
        }
        return null;
    }

    public String generateStructuredJson(String systemPrompt, String userMessage) {
        String enhancedSystemPrompt = systemPrompt + "\nCRITICAL: You must return ONLY raw valid JSON without markdown fences (no ```json or ```).";
        String raw = generateResponse(enhancedSystemPrompt, userMessage);
        if (raw == null) return null;

        // Clean any accidental markdown code fences
        String cleaned = raw.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        return cleaned.trim();
    }
}
