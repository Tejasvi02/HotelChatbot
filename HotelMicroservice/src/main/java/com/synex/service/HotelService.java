package com.synex.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.synex.domain.Hotel;
import com.synex.repository.HotelRepository;

@Service
public class HotelService {

    @Autowired
    private HotelRepository hotelRepository;
    
    public List<Hotel> getAllHotels(){
    	return hotelRepository.findAll();
    }

    public List<Hotel> searchHotels(String query, Integer noRooms, Integer noGuests, LocalDate checkInDate, LocalDate checkOutDate) {
        // For now, just do a keyword search on hotelName, city, or state
        return hotelRepository.findByHotelNameContainingIgnoreCaseOrCityContainingIgnoreCaseOrStateContainingIgnoreCase(
            query, query, query
        );
        
        // Later, you can extend this method to filter by rooms, guests, and dates as needed.
    }
}
