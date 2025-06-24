package com.synex.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synex.service.AIService;
import com.synex.service.OpenAiService;

@RestController
@RequestMapping("/ai")
public class AIController {

	@Autowired
    private OpenAiService openAiService;
	
	@Autowired
    private AIService aiService;
	
    @PostMapping("/chat")
    public ResponseEntity<?> chatWithBot(@RequestBody String userInput) {
        if (aiService.isHotelQuery(userInput)) {
            String hotelDetails = aiService.getHotelResponseFromAI(userInput);
            return ResponseEntity.ok(hotelDetails);  // Full hotel info
        } else {
            return ResponseEntity.ok(openAiService.askOpenAi(userInput));
        }
    }

    
}


