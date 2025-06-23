package com.synex.controller;

import com.synex.service.HotelEmbeddingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/hotels")
public class HotelEmbeddingController {

    @Autowired
    private HotelEmbeddingService embeddingService;

    // 1. Save vector for a hotel
//    @PostMapping("/embed")
//    public ResponseEntity<String> embedHotel(@RequestBody Map<String, String> payload) {
//        //int hotelId = Integer.parseInt(payload.get("hotel_id"));
//    	Object hotelIdObj = payload.get("hotelId");
//    	if (hotelIdObj == null) {
//    	    return ResponseEntity.badRequest().body("Missing 'hotelId' in request body");
//    	}
//
//    	int hotelId;
//    	try {
//    	    hotelId = Integer.parseInt(hotelIdObj.toString());
//    	} catch (NumberFormatException e) {
//    	    return ResponseEntity.badRequest().body("'hotelId' must be a valid integer");
//    	}
//
//        String description = payload.get("description");
//
//        embeddingService.embedAndSaveHotel(hotelId, description);
//        return ResponseEntity.ok("Hotel vector saved.");
//    }
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
    public ResponseEntity<?> findSimilarHotels(@RequestBody Map<String, String> payload) {
        String query = payload.get("query");
        return ResponseEntity.ok(embeddingService.findSimilarHotelIdsWithKeyword(query));
    }
}

