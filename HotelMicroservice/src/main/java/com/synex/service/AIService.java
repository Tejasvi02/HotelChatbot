package com.synex.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AIService {

    public String getAIResponse(String userMessage) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(userMessage, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:8383/ai/chat", // Replace PORT with aiService's port
                request,
                String.class
            );
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to get a response from the AI service.";
        }
    }
}
