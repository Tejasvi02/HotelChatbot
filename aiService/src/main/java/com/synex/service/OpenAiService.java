package com.synex.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class OpenAiService {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    public String askOpenAi(String userInput) {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper mapper = new ObjectMapper();

        // Build JSON payload safely
        ObjectNode payload = mapper.createObjectNode();
        payload.put("model", "gpt-4o-mini");

        ArrayNode messages = mapper.createArrayNode();

        ObjectNode systemMessage = mapper.createObjectNode();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are a helpful assistant.");

        ObjectNode userMessage = mapper.createObjectNode();
        userMessage.put("role", "user");
        userMessage.put("content", userInput);

        messages.add(systemMessage);
        messages.add(userMessage);

        payload.set("messages", messages);

        // Setup headers
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(openAiApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create request
        HttpEntity<String> request;
        try {
            String jsonPayload = mapper.writeValueAsString(payload);
            request = new HttpEntity<>(jsonPayload, headers);
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to build request.";
        }

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                "https://api.openai.com/v1/chat/completions",
                request,
                String.class
            );

            JsonNode root = mapper.readTree(response.getBody());
            return root.path("choices").get(0).path("message").path("content").asText();

        } catch (Exception e) {
            e.printStackTrace();
            return "Error talking to OpenAI API.";
        }
    }
}
