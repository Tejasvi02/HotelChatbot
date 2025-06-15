package com.synex.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AIService {

	RestTemplate restTemplate = new RestTemplate();

    private final String AI_SERVICE_URL = "http://localhost:8383/ai/chat";

    public String getAIResponse(String userMessage) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(userMessage, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(AI_SERVICE_URL, request, String.class);
        return response.getBody();
    }
}

