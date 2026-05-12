package com.cts.mfrp.au.service;

import com.cts.mfrp.au.dto.AiRiskResponse;
import com.cts.mfrp.au.model.Product;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiRiskService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.model}")
    private String model;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiRiskService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5_000);   // fail fast if the network drops the TCP handshake
        factory.setReadTimeout(30_000);     // 30s for Gemini to respond
        this.restTemplate = new RestTemplate(factory);
    }

    public AiRiskResponse analyseProduct(Product product) {
        if (apiKey == null || apiKey.isBlank()) {
            return AiRiskResponse.withError("AI service is not configured. Set GEMINI_API_KEY.");
        }

        String prompt = buildPrompt(product);
        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent?key=" + apiKey;

        try {
            String requestBody = objectMapper.writeValueAsString(buildRequestPayload(prompt));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            String response = restTemplate.postForObject(url, entity, String.class);
            return parseGeminiResponse(response);
        } catch (org.springframework.web.client.ResourceAccessException e) {
            return AiRiskResponse.withError(
                "Cannot reach Gemini (network blocked or offline). Try again on the deployed environment.");
        } catch (Exception e) {
            return AiRiskResponse.withError("AI check failed: " + e.getMessage());
        }
    }

    private String buildPrompt(Product product) {
        String name = nullSafe(product.getProductName());
        String description = nullSafe(product.getDescription());
        String category = product.getCategory() != null ? nullSafe(product.getCategory().getCategoryName()) : "Unknown";
        String seller = product.getSeller() != null ? nullSafe(product.getSeller().getName()) : "Unknown";
        float price = product.getStartingPrice();

        return "You are reviewing a product submission for an online auction platform. "
             + "Be conservative — only flag clear, specific concerns.\n\n"
             + "Product details:\n"
             + "- Name: " + name + "\n"
             + "- Description: " + description + "\n"
             + "- Category: " + category + "\n"
             + "- Starting Price: INR " + price + "\n"
             + "- Seller: " + seller + "\n\n"
             + "Return STRICT JSON only, no markdown, no code fences:\n"
             + "{\n"
             + "  \"riskScore\": <integer 0-100, higher = more suspicious>,\n"
             + "  \"flags\": [\"short specific concern\", \"...\"],\n"
             + "  \"summary\": \"one sentence assessment\"\n"
             + "}";
    }

    private java.util.Map<String, Object> buildRequestPayload(String prompt) {
        java.util.Map<String, Object> part = new java.util.HashMap<>();
        part.put("text", prompt);

        java.util.Map<String, Object> content = new java.util.HashMap<>();
        content.put("parts", List.of(part));

        java.util.Map<String, Object> generationConfig = new java.util.HashMap<>();
        generationConfig.put("temperature", 0.1);
        generationConfig.put("responseMimeType", "application/json");

        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("contents", List.of(content));
        payload.put("generationConfig", generationConfig);
        return payload;
    }

    private AiRiskResponse parseGeminiResponse(String raw) {
        try {
            JsonNode root = objectMapper.readTree(raw);
            JsonNode text = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
            if (text.isMissingNode()) {
                return AiRiskResponse.withError("Unexpected Gemini response shape.");
            }

            JsonNode inner = objectMapper.readTree(text.asText());
            int score = inner.path("riskScore").asInt(-1);
            String summary = inner.path("summary").asText("");
            List<String> flags = new ArrayList<>();
            JsonNode flagsNode = inner.path("flags");
            if (flagsNode.isArray()) {
                for (JsonNode f : flagsNode) flags.add(f.asText());
            }
            if (score < 0 || score > 100) {
                return AiRiskResponse.withError("Model returned an invalid risk score.");
            }
            return new AiRiskResponse(score, flags, summary);
        } catch (Exception e) {
            return AiRiskResponse.withError("Could not parse AI response: " + e.getMessage());
        }
    }

    private String nullSafe(String s) {
        return s == null ? "" : s;
    }
}
