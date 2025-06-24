package com.synex.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


@RestController
@RequestMapping("/bot")
public class HotelBotController {

    @PostMapping("/ask")
    public ResponseEntity<String> askBot(@RequestBody String userMessage) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(userMessage, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
            "http://localhost:8383/ai/chat",
            request,
            String.class
        );

        return ResponseEntity.ok(response.getBody());
    }

}

