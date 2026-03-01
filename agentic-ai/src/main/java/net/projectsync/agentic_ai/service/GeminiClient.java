package net.projectsync.agentic_ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.time.Duration;

/**
 * HTTP client responsible for all communication with the Gemini API.
 *
 * Responsibilities:
 *  - Build the correct JSON request format Gemini expects
 *  - Send the HTTP POST request
 *  - Parse and return just the text from the response
 *  - Surface meaningful error messages when things go wrong
 *
 * This class knows nothing about goals, plans, or sessions —
 * it only knows how to talk to Gemini.
 */
@Service
public class GeminiClient {

    // The Gemini model endpoint. gemini-2.5-flash is available on the free tier.
    private static final String URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    private final String apiKey;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper; // handles JSON serialization/deserialization

    /**
     * @param apiKey       Injected from application.properties: gemini.api.key=...
     * @param builder      Spring's RestTemplate factory — used to set timeouts
     * @param objectMapper Spring's shared Jackson instance for JSON handling
     */
    public GeminiClient(
            @Value("${gemini.api.key}") String apiKey,
            RestTemplateBuilder builder,
            ObjectMapper objectMapper) {

        this.apiKey = apiKey;
        this.objectMapper = objectMapper;

        // Configure timeouts so a slow/hung Gemini call doesn't block forever.
        // connectTimeout: how long to wait to establish the connection (5s)
        // readTimeout:    how long to wait for Gemini to respond (60s — AI can be slow)
        this.restTemplate = builder
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(60))
                .build();
    }

    /**
     * Sends a prompt to Gemini and returns the generated text.
     *
     * @param prompt  The instruction or question to send
     * @return        Gemini's response as plain text
     */
    public String generate(String prompt) {

        // Step 1: Wrap the prompt in the JSON structure Gemini expects
        String requestBody = buildRequestBody(prompt);

        // Step 2: Set the Content-Type header so Gemini knows we're sending JSON
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Step 3: Combine the body + headers into one HTTP entity
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        // Step 4: Append the API key as a query parameter
        String fullUrl = URL + "?key=" + apiKey;

        try {
            // Step 5: Send the POST request and receive the full HTTP response
            ResponseEntity<String> response = restTemplate.postForEntity(fullUrl, entity, String.class);

            // Step 6: Pull just the generated text out of the response JSON
            return extractText(response.getBody());

        } catch (HttpClientErrorException ex) {
            // 4xx errors (e.g. 429 Too Many Requests, 400 Bad Request)
            // Include both the status code and the response body so the error is actionable
            throw new RuntimeException(
                    "Gemini API error: " + ex.getStatusCode() + " — " + ex.getResponseBodyAsString(), ex);
        } catch (Exception ex) {
            // Network failures, timeouts, or anything unexpected
            throw new RuntimeException("Gemini API call failed", ex);
        }
    }

    /**
     * Builds the JSON body in the format Gemini's API expects.
     *
     * Gemini expects this structure:
     * {
     *   "contents": [
     *     { "parts": [ { "text": "your prompt here" } ] }
     *   ]
     * }
     *
     * We use Jackson (ObjectMapper) to build this safely —
     * never manually concatenate strings into JSON, as special
     * characters like quotes or newlines will break the request.
     */
    private String buildRequestBody(String prompt) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            ArrayNode contents = root.putArray("contents");
            ObjectNode content = contents.addObject();
            ArrayNode parts = content.putArray("parts");
            parts.addObject().put("text", prompt);
            return objectMapper.writeValueAsString(root);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to serialize Gemini request", ex);
        }
    }

    /**
     * Extracts the plain text from Gemini's response JSON.
     *
     * Gemini wraps the actual answer in a deeply nested structure:
     * {
     *   "candidates": [
     *     {
     *       "content": {
     *         "parts": [
     *           { "text": "← this is what we want" }
     *         ]
     *       }
     *     }
     *   ]
     * }
     *
     * This method navigates that structure and returns just the text.
     */
    private String extractText(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            return root
                    .path("candidates").get(0)    // first candidate response
                    .path("content")
                    .path("parts").get(0)         // first part of the content
                    .path("text")
                    .asText()
                    .trim();
        } catch (Exception ex) {
            // Include the raw response body so you can see what went wrong
            throw new RuntimeException("Failed to parse Gemini response: " + responseBody, ex);
        }
    }
}