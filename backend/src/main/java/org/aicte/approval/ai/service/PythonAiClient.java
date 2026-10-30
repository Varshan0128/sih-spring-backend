package org.aicte.approval.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Component
@Slf4j
public class PythonAiClient {

    private final RestTemplate restTemplate;

    @Value("${app.ai-service.base-url}")
    private String baseUrl;

    @Value("${app.ai-service.api-key:}")
    private String apiKey;

    public PythonAiClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(10000);
        this.restTemplate = new RestTemplate(factory);
    }

    public JsonNode post(String path, Object payload) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("AI_SERVICE_API_KEY is not configured");
        }

        String normalizedBaseUrl = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Internal-API-Key", apiKey);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                normalizedBaseUrl + path,
                HttpMethod.POST,
                new HttpEntity<>(payload, headers),
                JsonNode.class
        );

        if (response.getBody() == null) {
            throw new IllegalStateException("Python AI service returned an empty response");
        }
        return response.getBody();
    }
}
