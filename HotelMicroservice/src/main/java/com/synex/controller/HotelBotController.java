package com.synex.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synex.service.AIService;

@RestController
@RequestMapping("/bot")
public class HotelBotController {

    @Autowired
    private AIService aiService;

    @PostMapping("/ask")
    public ResponseEntity<String> askBot(@RequestBody String userMessage) {
        String response = aiService.getAIResponse(userMessage);
        return ResponseEntity.ok(response);
    }
}

