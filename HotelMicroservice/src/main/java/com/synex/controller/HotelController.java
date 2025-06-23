package com.synex.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.synex.domain.Hotel;
import com.synex.domain.SearchDetails;
import com.synex.service.HotelService;

@RestController
@RequestMapping("/hotels")
public class HotelController {
	
	@Autowired
	private HotelService hotelService;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@GetMapping("/all")
	public List<Hotel> GetAllHotels(){
		return hotelService.getAllHotels();
	}
	
	@PostMapping("/search")
	public ResponseEntity<List<Hotel>> searchHotels(@RequestBody SearchDetails searchDetails) {
	    System.out.println("Received search: " + searchDetails);  // after adding toString()

	    try {
	        List<Hotel> hotels = hotelService.searchHotels(
	            searchDetails.getSearchHotel(),
	            searchDetails.getNoOfRooms(),
	            searchDetails.getNoOfGuests()
	        );
	        return ResponseEntity.ok(hotels);
	    } catch (Exception e) {
	        e.printStackTrace(); // Log exact problem
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	    }
	}
	
	//embedding all hotels at once
	@Value("${ai.service.url}") // in application.properties
	private String aiServiceUrl;

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
	                hotel.getDescription(),
	                hotel.getEmail(), 
	                hotel.getMobile(), 
	                hotel.getTimesBooked()
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

}
