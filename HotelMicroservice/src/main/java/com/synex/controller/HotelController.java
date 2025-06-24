package com.synex.controller;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
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
    
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
//
//    @PostMapping("/detailsByIdsAndKeyword")
//    public ResponseEntity<?> getHotelDetailsByIdsAndKeyword(@RequestBody Map<String, Object> payload) {
//        List<Integer> hotelIds = (List<Integer>) payload.get("hotelIds");
//        String keyword = (String) payload.get("query");
//        System.out.println("Filtering with query: '" + keyword + "'");
//        System.out.println("Hotel IDs passed: " + hotelIds);
//
//        if (hotelIds == null || hotelIds.isEmpty() || keyword == null || keyword.trim().isEmpty()) {
//            return ResponseEntity.badRequest().body("Missing hotelIds or query");
//        }
//        keyword = keyword.replace("\"", "").trim();
//        // Tokenize and prepare LIKE conditions
//        String[] tokens = keyword.toLowerCase().split("\\s+"); // e.g. ["seaside", "resort"]
//        StringBuilder whereClause = new StringBuilder();
//        Map<String, Object> params = new HashMap<>();
//
//        // Base condition for hotel ID filtering
//        whereClause.append("hotel_id IN (:hotelIds) AND (");
//
//        for (int i = 0; i < tokens.length; i++) {
//            String token = tokens[i];
//            String paramName = "kw" + i;
//            String likePattern = "%" + token + "%";
//
//            // For each field to match token
//            if (i > 0) whereClause.append(" AND ");
//            whereClause.append("(")
//                .append("LOWER(hotel_name) LIKE :").append(paramName)
//                .append(" OR LOWER(description) LIKE :").append(paramName)
//                .append(" OR LOWER(state) LIKE :").append(paramName)
//                .append(" OR LOWER(city) LIKE :").append(paramName)
//                .append(" OR CAST(average_price AS TEXT) LIKE :").append(paramName)
//                .append(")");
//
//            params.put(paramName, likePattern);
//        }
//
//        whereClause.append(")");
//
//        String sql = "SELECT hotel_id, hotel_name, description, city, state, average_price, star_rating " +
//                     "FROM hotels WHERE " + whereClause;
//
//        params.put("hotelIds", hotelIds);
//
//        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(sql, params);
//
//        return ResponseEntity.ok(result);
//    }
    
    @PostMapping("/detailsByIdsAndKeyword")
    public ResponseEntity<?> getHotelDetailsByIdsAndKeyword(@RequestBody Map<String, Object> payload) {
        List<Integer> hotelIds = (List<Integer>) payload.get("hotelIds");
        String keyword = (String) payload.get("query");

        if (hotelIds == null || hotelIds.isEmpty() || keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Missing hotelIds or query");
        }

        keyword = keyword.replace("\"", "").trim();
        String[] tokens = keyword.toLowerCase().split("\\s+");

        StringBuilder whereClause = new StringBuilder();
        Map<String, Object> params = new HashMap<>();
        whereClause.append(" h.hotel_id IN (:hotelIds) AND (");  // <-- FIXED: added leading space

        boolean addedCondition = false;
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            String paramName = "kw" + i;

            if (token.matches("\\d+")) {
                // Numeric token treated as budget
                if (addedCondition) whereClause.append(" AND ");
                whereClause.append("h.average_price <= :").append(paramName);
                params.put(paramName, Double.parseDouble(token));
            } else {
                if (addedCondition) whereClause.append(" AND (");
                else whereClause.append(" (");

                whereClause.append("LOWER(h.hotel_name) LIKE :").append(paramName)
                           .append(" OR LOWER(h.description) LIKE :").append(paramName)
                           .append(" OR LOWER(h.state) LIKE :").append(paramName)
                           .append(" OR LOWER(h.city) LIKE :").append(paramName)
                           .append(" OR LOWER(a.name) LIKE :").append(paramName)
                           .append(")");

                params.put(paramName, "%" + token + "%");
            }

            addedCondition = true;
        }

        whereClause.append(")");  // closing last condition group

        // Final SQL query using LEFT JOIN to include amenities
        String sql = """
            SELECT DISTINCT h.hotel_id, h.hotel_name, h.description, h.city, h.state, 
                            h.average_price, h.star_rating
            FROM hotels h
            LEFT JOIN hotels_amenities ha ON h.hotel_id = ha.hotel_hotel_id
            LEFT JOIN amenities a ON ha.amenities_a_id = a.a_id
            WHERE """ + whereClause.toString();  // safe now due to leading space

        params.put("hotelIds", hotelIds);

        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(sql, params);
        return ResponseEntity.ok(result);
    }


}
