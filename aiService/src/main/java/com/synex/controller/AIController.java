package com.synex.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synex.service.OpenAiService;

@RestController
@RequestMapping("/ai")
public class AIController {

	@Autowired
    private OpenAiService openAiService;
	
    @PostMapping("/chat")
    public ResponseEntity<String> chatWithBot(@RequestBody String userInput) {
        // Placeholder for AI logic, replace with actual AI model call or rules
        //String response = "Hello! You asked: " + userInput;
    	String response = openAiService.askOpenAi(userInput);
        return ResponseEntity.ok(response);
    }
}

