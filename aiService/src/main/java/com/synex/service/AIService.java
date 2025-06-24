package com.synex.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AIService {

    @Autowired
    private HotelEmbeddingService hotelEmbeddingService;

    public String getHotelResponseFromAI(String userMessage) {
    	System.out.println("User query (raw): '" + userMessage + "'");
        try {
            List<Map<String, Object>> hotelData = hotelEmbeddingService.getFinalFilteredHotels(userMessage);
            if (hotelData == null || hotelData.isEmpty()) {
                return "Sorry, I couldn't find any hotels matching your query.";
            }
            return formatHotelList(hotelData);
        } catch (Exception e) {
            e.printStackTrace();
            return "Something went wrong fetching hotel data.";
        }
    }

    private String formatHotelList(List<Map<String, Object>> hotels) {
        StringBuilder reply = new StringBuilder("Here are some hotels I found:\n\n");
        for (Map<String, Object> hotel : hotels) {
            String name = (String) hotel.get("hotel_name");
            String city = (String) hotel.get("city");
            String state = (String) hotel.get("state");
            double price = (hotel.get("average_price") instanceof Number)
                    ? ((Number) hotel.get("average_price")).doubleValue()
                    : 0.0;
            int stars = (hotel.get("star_rating") instanceof Number)
                    ? ((Number) hotel.get("star_rating")).intValue()
                    : 0;

            reply.append(String.format("🏨 %s (%d★)\n📍 %s, %s\n💵 $%.2f/night\n\n",
                    name, stars, city, state, price));
        }
        return reply.toString();
    }

    public boolean isHotelQuery(String message) {
        String lower = message.toLowerCase();
        return lower.contains("hotel") || lower.contains("resort") ||
               lower.contains("stay") || lower.contains("lodging") ||
               lower.contains("book a place") || lower.contains("room");
    }
}
