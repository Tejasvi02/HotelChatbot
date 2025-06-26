package com.synex.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class BookingAIService {

    private final Map<String, BookingSession> activeSessions = new HashMap<>();

    private enum Step {
        HOTEL_NAME, ROOM_TYPE, NUM_ROOMS, NUM_GUESTS, CHECKIN_DATE, CHECKOUT_DATE, CONFIRM
    }

    private static class BookingSession {
        BookingRequest bookingRequest = new BookingRequest();
        Step currentStep = Step.HOTEL_NAME;
    }

    @Autowired
    private RestTemplate restTemplate;

    public boolean hasActiveBookingSession(String sessionId) {
        return activeSessions.containsKey(sessionId);
    }

    public String handleBookingRequest(String userMessage, String sessionId) {
        userMessage = userMessage.strip();
        if (userMessage.startsWith("\"") && userMessage.endsWith("\"") && userMessage.length() > 1) {
            userMessage = userMessage.substring(1, userMessage.length() - 1);
        }

        System.out.println("Incoming message: " + userMessage);

        if (activeSessions.containsKey(sessionId)) {
            System.out.println("Continuing existing session for user.");
            return processBookingStep(activeSessions.get(sessionId), userMessage, sessionId);
        }

        String lowerMessage = userMessage.toLowerCase(Locale.ROOT).strip();
        System.out.println("Sanitized lowerMessage: [" + lowerMessage + "]");

        BookingRequest booking = parseBookingRequest(userMessage);
        if (booking != null) {
            System.out.println("Parsed booking: " + booking);
            Integer hotelId = fetchHotelIdByNameUsingVectorSearch(booking.getHotelName(), null);
            if (hotelId == null) {
                return "Sorry, I couldn't find a hotel with the name: " + booking.getHotelName();
            }
            booking.setHotelId(hotelId);
            booking.setUsername(sessionId);
            return submitBooking(booking, null);
        }

        if (lowerMessage.startsWith("book")) {
            String hotelName = userMessage.replaceFirst("(?i)book\\s*", "").strip();
            System.out.println("Parsed hotel name: " + hotelName);

            if (hotelName.isEmpty()) {
                return "Please specify a hotel name to begin the booking.";
            }

            BookingSession session = new BookingSession();
            session.bookingRequest.setHotelName(hotelName);
            session.currentStep = Step.NUM_ROOMS;
            activeSessions.put(sessionId, session);

            return "How many rooms do you need?";
        }

        System.out.println("Could not parse booking.");
        return "Sorry, I couldn't understand the booking request.";
    }

    private String processBookingStep(BookingSession session, String userInput, String sessionId) {
        try {
            switch (session.currentStep) {
                case HOTEL_NAME:
                    session.bookingRequest.setHotelName(userInput);
                    session.currentStep = Step.NUM_ROOMS;
                    return "How many rooms do you need?";

                case NUM_ROOMS:
                    session.bookingRequest.setNumRooms(Integer.parseInt(userInput.trim()));
                    session.currentStep = Step.ROOM_TYPE;
                    return "Please reply with 'Deluxe' or 'Standard' to choose your room type.";

                case ROOM_TYPE:
                    String inputLower = userInput.trim().toLowerCase();
                    if (inputLower.equals("deluxe") || inputLower.equals("standard")) {
                        session.bookingRequest.setRoomType(
                            inputLower.substring(0, 1).toUpperCase() + inputLower.substring(1));
                        session.currentStep = Step.NUM_GUESTS;
                        return "How many guests in total?";
                    } else {
                        return "Please reply with 'Deluxe' or 'Standard' to choose your room type.";
                    }

                case NUM_GUESTS:
                    session.bookingRequest.setNumGuests(Integer.parseInt(userInput.trim()));
                    session.currentStep = Step.CHECKIN_DATE;
                    return "Enter check-in date (MM/dd/yyyy):";

                case CHECKIN_DATE:
                    session.bookingRequest.setCheckInDate(parseDate(userInput));
                    session.currentStep = Step.CHECKOUT_DATE;
                    return "Enter check-out date (MM/dd/yyyy):";

                case CHECKOUT_DATE:
                    session.bookingRequest.setCheckOutDate(parseDate(userInput));
                    session.currentStep = Step.CONFIRM;
                    return "Please type 'Confirm' to proceed with booking or 'Cancel' to abort.";

                case CONFIRM:
                    if (userInput.equalsIgnoreCase("confirm")) {
                        Integer hotelId = fetchHotelIdByNameUsingVectorSearch(session.bookingRequest.getHotelName(), null);
                        if (hotelId == null) {
                            activeSessions.remove(sessionId);
                            return "Sorry, I couldn't find the hotel. Please try again.";
                        }
                        session.bookingRequest.setHotelId(hotelId);
                        String response = submitBooking(session.bookingRequest, null);
                        activeSessions.remove(sessionId);
                        return response;
                    } else {
                        activeSessions.remove(sessionId);
                        return "Booking cancelled. You can start again by saying 'book [HotelName]'.";
                    }
            }
        } catch (Exception e) {
            activeSessions.remove(sessionId);
            return "Invalid input or error occurred. Please start the booking again.";
        }
        return "Unexpected error. Please try again.";
    }

    private String processBookingStep(BookingSession session, String userInput, String userKey, String jwtToken) {
        try {
            switch (session.currentStep) {
                case HOTEL_NAME:
                    session.bookingRequest.setHotelName(userInput);
                    session.currentStep = Step.NUM_ROOMS;
                    return "How many rooms do you need?";

                case NUM_ROOMS:
                    session.bookingRequest.setNumRooms(Integer.parseInt(userInput.trim()));
                    session.currentStep = Step.ROOM_TYPE;
                    return "Please reply with 'Deluxe' or 'Standard' to choose your room type.";

                case ROOM_TYPE:
                    String inputLower = userInput.trim().toLowerCase();
                    if (inputLower.equals("deluxe") || inputLower.equals("standard")) {
                        session.bookingRequest.setRoomType(
                            inputLower.substring(0, 1).toUpperCase() + inputLower.substring(1));
                        session.currentStep = Step.NUM_GUESTS;
                        return "How many guests in total?";
                    } else {
                        return "Please reply with 'Deluxe' or 'Standard' to choose your room type.";
                    }

                case NUM_GUESTS:
                    session.bookingRequest.setNumGuests(Integer.parseInt(userInput.trim()));
                    session.currentStep = Step.CHECKIN_DATE;
                    return "Enter check-in date (MM/dd/yyyy):";

                case CHECKIN_DATE:
                    session.bookingRequest.setCheckInDate(parseDate(userInput));
                    session.currentStep = Step.CHECKOUT_DATE;
                    return "Enter check-out date (MM/dd/yyyy):";

                case CHECKOUT_DATE:
                    session.bookingRequest.setCheckOutDate(parseDate(userInput));
                    session.currentStep = Step.CONFIRM;
                    return "Please type 'Confirm' to proceed with booking or 'Cancel' to abort.";

                case CONFIRM:
                    if (userInput.equalsIgnoreCase("confirm")) {
                        Integer hotelId = fetchHotelIdByNameUsingVectorSearch(session.bookingRequest.getHotelName(), jwtToken);
                        if (hotelId == null) {
                            activeSessions.remove(userKey);
                            return "Sorry, I couldn't find the hotel. Please try again.";
                        }
                        session.bookingRequest.setHotelId(hotelId);
                        String response = submitBooking(session.bookingRequest, jwtToken);
                        activeSessions.remove(userKey);
                        return response;
                    } else {
                        activeSessions.remove(userKey);
                        return "Booking cancelled. You can start again by saying 'book [HotelName]'.";
                    }
            }
        } catch (Exception e) {
            activeSessions.remove(userKey);
            return "Invalid input or error occurred. Please start the booking again.";
        }
        return "Unexpected error. Please try again.";
    }

    private String parseDate(String input) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        LocalDate date = LocalDate.parse(input.trim(), formatter);
        return date.toString();
    }

    private String submitBooking(BookingRequest booking, String jwtToken) {
        booking.setUsername("user"); // Optionally decode from JWT

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
    }

    public boolean isBookingQuery(String userMessage) {
        String msg = userMessage.toLowerCase();
        return msg.contains("book") || activeSessions.containsKey(userMessage);
    }

    private BookingRequest parseBookingRequest(String message) {
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
            request.setUsername("user");
            request.setNumGuests(2);

            return request;
        }
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
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String extractUserKey(String jwtToken) {
        return jwtToken != null ? jwtToken : UUID.randomUUID().toString();
    }

    // --- DTO ---
    public static class BookingRequest {
        private int hotelId;
        private String hotelName;
        private int numRooms;
        private int numGuests;
        private String checkInDate;
        private String checkOutDate;
        private String username;
        private String roomType;

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
        
        public String getRoomType() { return roomType; }
        public void setRoomType(String roomType) { this.roomType = roomType; }
    }
}
