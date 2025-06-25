package com.synex.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.synex.domain.Hotel;

public interface HotelRepository extends JpaRepository<Hotel, Integer> {
	
	List<Hotel> findByHotelNameContainingIgnoreCaseOrCityContainingIgnoreCaseOrStateContainingIgnoreCase(
		    String hotelName, String city, String state
		);
	Hotel findByHotelNameIgnoreCase(String hotelName);
}
