package com.synex.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class BookingAIService {

    @Autowired
    private RestTemplate restTemplate;

    public String handleBookingRequest(String userMessage, String jwtToken) {
        try {
            BookingRequest booking = parseBookingRequest(userMessage);
            if (booking == null) return "Sorry, I couldn't understand the booking request.";

            Integer hotelId = fetchHotelIdByNameUsingVectorSearch(booking.getHotelName(), jwtToken);
            System.out.println(hotelId);
            if (hotelId == null) {
                return "Sorry, I couldn't find a hotel with the name: " + booking.getHotelName();
            }

            booking.setHotelId(hotelId);
            // keep hotelName too, so hotel microservice can fallback if needed

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(jwtToken);
            HttpEntity<BookingRequest> entity = new HttpEntity<>(booking, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:8282/bookings/create",
                HttpMethod.POST,
                entity,
                String.class
            );

            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to handle booking: " + e.getMessage();
        }
    }
    
    public boolean isBookingQuery(String userMessage) {
        String msg = userMessage.toLowerCase();
        return msg.contains("book") && msg.contains("room");
    }

    private BookingRequest parseBookingRequest(String message) {
        System.out.println("Trying to extract booking request from: " + message);
        Pattern pattern = Pattern.compile(
            "book\\s+(\\d+)\\s+room[s]?\\s+(?:in\\s+(?:hotel\\s+)?)?(.+?)\\s+from\\s+(\\d{2}/\\d{2}/\\d{4})\\s+to\\s+(\\d{2}/\\d{2}/\\d{4})",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            int numRooms = Integer.parseInt(matcher.group(1));
            String hotelName = matcher.group(2).trim();
            String checkInStr = matcher.group(3).trim();
            String checkOutStr = matcher.group(4).trim();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            LocalDate checkInDate = LocalDate.parse(checkInStr, formatter);
            LocalDate checkOutDate = LocalDate.parse(checkOutStr, formatter);

            BookingRequest request = new BookingRequest();
            request.setHotelName(hotelName);
            request.setNumRooms(numRooms);
            request.setCheckInDate(checkInDate.toString());
            request.setCheckOutDate(checkOutDate.toString());
            request.setUsername("user"); // optionally replace with real user from JWT
            request.setNumGuests(2);     // default value

            System.out.println("✅ Extracted Booking:");
            System.out.println("Hotel: " + hotelName);
            System.out.println("Check-in: " + checkInDate);
            System.out.println("Check-out: " + checkOutDate);
            System.out.println("Rooms: " + numRooms);

            return request;
        }

        System.out.println("❌ No match found in message.");
        return null;
    }

    private Integer fetchHotelIdByNameUsingVectorSearch(String hotelName, String jwtToken) {
        String url = "http://localhost:8383/hotels/similarfirst";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (jwtToken != null) {
            headers.setBearerAuth(jwtToken);
        }

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("query", hotelName);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("hotel_id")) {
                Object idObj = responseBody.get("hotel_id");
                if (idObj instanceof Number) {
                    return ((Number) idObj).intValue();
                } else if (idObj instanceof String) {
                    return Integer.parseInt((String) idObj);
                }
            }

            System.err.println("❌ No hotel_id found in response.");
            return null;
        } catch (Exception e) {
            System.err.println("❌ Failed to fetch hotel ID using vector search for: " + hotelName);
            e.printStackTrace();
            return null;
        }
    }

    // Inner DTO
    public static class BookingRequest {
        private int hotelId;
        private String hotelName;
        private int numRooms;
        private int numGuests;
        private String checkInDate;
        private String checkOutDate;
        private String username;

        public int getHotelId() { return hotelId; }
        public void setHotelId(int hotelId) { this.hotelId = hotelId; }

        public String getHotelName() { return hotelName; }
        public void setHotelName(String hotelName) { this.hotelName = hotelName; }

        public int getNumRooms() { return numRooms; }
        public void setNumRooms(int numRooms) { this.numRooms = numRooms; }

        public int getNumGuests() { return numGuests; }
        public void setNumGuests(int numGuests) { this.numGuests = numGuests; }

        public String getCheckInDate() { return checkInDate; }
        public void setCheckInDate(String checkInDate) { this.checkInDate = checkInDate; }

        public String getCheckOutDate() { return checkOutDate; }
        public void setCheckOutDate(String checkOutDate) { this.checkOutDate = checkOutDate; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }
}
