package com.synex.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synex.service.AIService;
import com.synex.service.BookingAIService;
import com.synex.service.FaqEmbeddingService;
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
    
    @Autowired
    private FaqEmbeddingService faqEmbeddingService;

    private String extractSessionId(HttpServletRequest request) {
        String sessionId = request.getHeader("X-Session-Id");
        if (sessionId == null || sessionId.isEmpty()) {
            // fallback or generate new session ID (optional)
            sessionId = "default-session-id"; // or UUID.randomUUID().toString();
        }
        return sessionId;
    }
    

    @PostMapping("/chat")
    public ResponseEntity<?> chatWithBot(@RequestBody String userInput, HttpServletRequest request) {
        String sessionId = extractSessionId(request);
        String faqAnswer = faqEmbeddingService.getMatchingFaqAnswer(userInput);
        

        if (bookingAIService.hasActiveBookingSession(sessionId)) {
            String bookingResult = bookingAIService.handleBookingRequest(userInput, sessionId);
            return ResponseEntity.ok(bookingResult);
        } else if (bookingAIService.isBookingQuery(userInput)) {
            String bookingResult = bookingAIService.handleBookingRequest(userInput, sessionId);
            return ResponseEntity.ok(bookingResult);
        } else if (aiService.isHotelQuery(userInput)) {
            String hotelDetails = aiService.getHotelResponseFromAI(userInput, sessionId);
            return ResponseEntity.ok(hotelDetails + "<br>To <b>BOOK</b> any of these please type book followed by the hotel name");
        } else if (faqAnswer != null) {
                return ResponseEntity.ok(faqAnswer);
            } else {
                return ResponseEntity.ok(openAiService.askOpenAi(userInput));
            }
        }
    }



