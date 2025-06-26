package com.synex.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
    public ResponseEntity<?> chatWithBot(
            @RequestBody String userInput,
            HttpServletRequest request,
            @RequestHeader(value = "X-Language", required = false, defaultValue = "en") String userLang) {

        // Step 1: Translate input to English if needed
        String englishInput = openAiService.translateIfNeeded(userInput, userLang);

        // Step 2: Keep your existing logic unchanged, using englishInput instead of userInput
        String sessionId = extractSessionId(request);
        String faqAnswer = faqEmbeddingService.getMatchingFaqAnswer(englishInput);

        String botResponse;

        if (bookingAIService.hasActiveBookingSession(sessionId)) {
            botResponse = bookingAIService.handleBookingRequest(englishInput, sessionId);
        } else if (bookingAIService.isBookingQuery(englishInput)) {
            botResponse = bookingAIService.handleBookingRequest(englishInput, sessionId);
        } else if (aiService.isHotelQuery(englishInput)) {
            botResponse = aiService.getHotelResponseFromAI(englishInput, sessionId) +
                "<br>To <b>BOOK</b> any of these please type book followed by the hotel name";
        } else if (faqAnswer != null) {
            botResponse = faqAnswer;
        } else {
            botResponse = openAiService.askOpenAi(englishInput);
        }

        // (Optional) If you want botResponse back in userLang, you can translate it back here,
        // but since you didn’t specify that, I’ll leave it in English.

        return ResponseEntity.ok(botResponse);
    }

    }



