package com.synex.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.synex.domain.Hotel;
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
	
	@GetMapping("/search")
	public ResponseEntity<List<Hotel>> searchHotels(
	    @RequestParam String query,
	    @RequestParam(required = false) Integer noRooms,
	    @RequestParam(required = false) Integer noGuests,
	    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
	    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate
	) {
	    List<Hotel> results = hotelService.searchHotels(query, noRooms, noGuests, checkInDate, checkOutDate);
	    return ResponseEntity.ok(results);
	}
}
