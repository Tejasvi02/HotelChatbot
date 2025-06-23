package com.synex.controller;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import com.synex.domain.Hotel;
import com.synex.domain.SearchDetails;
import com.synex.service.HotelService;

@RestController
@RequestMapping("/hotels")
public class HotelController {

    @Autowired private HotelService hotelService;
    @Autowired private RestTemplate restTemplate;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Value("${ai.service.url}")
    private String aiServiceUrl;

    @GetMapping("/all")
    public List<Hotel> GetAllHotels() {
        return hotelService.getAllHotels();
    }

    @PostMapping("/search")
    public ResponseEntity<List<Hotel>> searchHotels(@RequestBody SearchDetails searchDetails) {
        try {
            List<Hotel> hotels = hotelService.searchHotels(
                searchDetails.getSearchHotel(),
                searchDetails.getNoOfRooms(),
                searchDetails.getNoOfGuests()
            );
            return ResponseEntity.ok(hotels);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/embed-all")
    public ResponseEntity<String> embedAllHotels() {
        List<Hotel> allHotels = hotelService.getAllHotels();

        for (Hotel hotel : allHotels) {
            try {
                String hotelText = """
                    Hotel Name: %s
                    Location: %s
                    Rating: %s
                    Price: %s
                    Description: %s
                    """.formatted(
                        hotel.getHotelName(),
                        hotel.getCity() + ", " + hotel.getState(),
                        hotel.getStarRating(),
                        hotel.getAveragePrice(),
                        hotel.getDescription()
                    );

                Map<String, String> payload = new HashMap<>();
                payload.put("hotelId", String.valueOf(hotel.getHotelId()));
                payload.put("description", hotelText);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);

                restTemplate.postForEntity(aiServiceUrl + "/hotels/embed", request, String.class);

            } catch (Exception e) {
                System.out.println("Failed to embed hotel: " + hotel.getHotelId() + " - " + e.getMessage());
            }
        }

        return ResponseEntity.ok("All hotels embedded.");
    }

    @PostMapping("/detailsByIdsAndKeyword")
    public ResponseEntity<?> getHotelDetailsByIdsAndKeyword(@RequestBody Map<String, Object> payload) {
        List<Integer> hotelIds = (List<Integer>) payload.get("hotelIds");
        String keyword = (String) payload.get("query");

        if (hotelIds == null || keyword == null) {
            return ResponseEntity.badRequest().body("Missing hotelIds or query");
        }

        String inClause = hotelIds.stream()
            .map(String::valueOf)
            .collect(Collectors.joining(","));

        String sql = String.format(
            "SELECT hotel_id, hotel_name, description, city, state, average_price, star_rating " +
            "FROM hotels WHERE hotel_id IN (%s) " +
            "AND (hotel_name ILIKE ? OR description ILIKE ? OR state ILIKE ? OR city ILIKE ? OR CAST(average_price AS TEXT) ILIKE ?)",
            inClause
        );

        String likeQuery = "%" + keyword + "%";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(
            sql, likeQuery, likeQuery, likeQuery, likeQuery, likeQuery
        );

        return ResponseEntity.ok(result);
    }

}
