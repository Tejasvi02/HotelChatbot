package com.synex.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.synex.domain.Hotel;
import com.synex.domain.SearchDetails;
import com.synex.service.HotelService;

@RestController
@RequestMapping("/hotels")
public class HotelController {
	
	@Autowired
	private HotelService hotelService;
	
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

}
