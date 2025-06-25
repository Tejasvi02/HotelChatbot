package com.synex.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synex.service.AIService;
import com.synex.service.BookingAIService;
import com.synex.service.OpenAiService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/ai")
public class AIController {

    @Autowired
    private AIService aiService;

    @Autowired
    private OpenAiService openAiService;
    
    @Autowired
    private BookingAIService bookingAIService;

    @PostMapping("/chat")
    public ResponseEntity<?> chatWithBot(@RequestBody String userInput, HttpServletRequest request) {
        String jwtToken = extractJwt(request);

        if (bookingAIService.isBookingQuery(userInput)) {
            String bookingResult = bookingAIService.handleBookingRequest(userInput, jwtToken);
            return ResponseEntity.ok(bookingResult);
        } else if (aiService.isHotelQuery(userInput)) {
            String hotelDetails = aiService.getHotelResponseFromAI(userInput, jwtToken);
            return ResponseEntity.ok(hotelDetails);
        } else {
            return ResponseEntity.ok(openAiService.askOpenAi(userInput));
        }
    }

    private String extractJwt(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        return (authHeader != null && authHeader.startsWith("Bearer ")) ? authHeader.substring(7) : null;
    }
}


