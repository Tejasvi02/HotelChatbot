package com.synex.controller;

import com.synex.service.HotelEmbeddingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/hotels")
public class HotelEmbeddingController {

    @Autowired
    private HotelEmbeddingService embeddingService;

    // 1. Save vector for a hotel
    @PostMapping("/embed")
    public ResponseEntity<String> embedHotel(@RequestBody Map<String, String> payload) {
    	System.out.println("This methos is being called in aiservice");
        try {
            Object hotelIdObj = payload.get("hotelId");
            if (hotelIdObj == null) {
                return ResponseEntity.badRequest().body("Missing 'hotelId' in request body");
            }

            int hotelId = Integer.parseInt(hotelIdObj.toString());
            String description = payload.get("description");

            System.out.println("Calling embedAndSaveHotel with hotelId=" + hotelId);
            embeddingService.embedAndSaveHotel(hotelId, description);
            return ResponseEntity.ok("Hotel vector saved.");
        } catch (Exception e) {
            System.out.println("Exception in embedHotel: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }


    // 2. Find similar hotels using embedding

    @PostMapping("/similar")
    public ResponseEntity<?> findSimilarHotels(
            @RequestBody Map<String, String> payload,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        
        String rawQuery = payload.get("query");
        if (rawQuery == null || rawQuery.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Query is missing.");
        }

        String query = rawQuery.replace("\"", "").trim();

        String jwtToken = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwtToken = authorizationHeader.substring(7);
        }

        return ResponseEntity.ok(embeddingService.findSimilarHotelIdsWithKeyword(query, jwtToken));
    }
    
    @PostMapping("/similarfirst")
    public ResponseEntity<?> findFirstSimilarHotel(
            @RequestBody Map<String, String> payload,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        String rawQuery = payload.get("query");
        if (rawQuery == null || rawQuery.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Query is missing.");
        }

        String query = rawQuery.replace("\"", "").trim();

        String jwtToken = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwtToken = authorizationHeader.substring(7);
        }

        Integer topHotelId = embeddingService.findTopSimilarHotelId(query);
        if (topHotelId == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No hotel found.");
        }

        return ResponseEntity.ok(Collections.singletonMap("hotel_id", topHotelId));
    }



}

