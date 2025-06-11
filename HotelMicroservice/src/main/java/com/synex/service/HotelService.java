package com.synex.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.synex.domain.Amenities;
import com.synex.domain.Hotel;
import com.synex.domain.HotelRoom;
import com.synex.repository.HotelRepository;

@Service
public class HotelService {

    @Autowired
    private HotelRepository hotelRepository;
    
    public List<Hotel> getAllHotels() {
        List<Hotel> hotels = hotelRepository.findAll();
        enrichHotelsWithAmenityNames(hotels);
        return hotels;
    }

    public List<Hotel> searchHotels(String keyword, int noOfRooms, int noOfGuests) {
        List<Hotel> hotels = hotelRepository.findByHotelNameContainingIgnoreCaseOrCityContainingIgnoreCaseOrStateContainingIgnoreCase(
            keyword, keyword, keyword
        );

        List<Hotel> filteredHotels = hotels.stream()
            .filter(hotel -> {
                int totalAvailableRooms = 0;
                int totalGuestCapacity = 0;

                for (HotelRoom hotelRoom : hotel.getHotelRooms()) {
                    int roomCount = hotelRoom.getNoRooms();
                    int roomCapacity = hotelRoom.getType() != null ? getCapacityByRoomType(hotelRoom.getType().getName()) : 1;

                    totalAvailableRooms += roomCount;
                    totalGuestCapacity += roomCount * roomCapacity;
                }

                boolean hasEnoughRooms = noOfRooms <= 0 || totalAvailableRooms >= noOfRooms;
                boolean hasEnoughCapacity = noOfGuests <= 0 || totalGuestCapacity >= noOfGuests;

                return hasEnoughRooms && hasEnoughCapacity;
            })
            .collect(Collectors.toList());

        enrichHotelsWithAmenityNames(filteredHotels);
        return filteredHotels;
    }
    
    private void enrichHotelsWithAmenityNames(List<Hotel> hotels) {
        for (Hotel hotel : hotels) {
            Set<String> amenityNames = new HashSet<>();

            // Hotel-level amenities
            if (hotel.getAmenities() != null) {
                for (Amenities amenity : hotel.getAmenities()) {
                    amenityNames.add(amenity.getName());
                }
            }

            // Room-level amenities
            if (hotel.getHotelRooms() != null) {
                for (HotelRoom room : hotel.getHotelRooms()) {
                    if (room.getAmenities() != null) {
                        for (Amenities amenity : room.getAmenities()) {
                            amenityNames.add(amenity.getName());
                        }
                    }
                }
            }

            hotel.setHotelAmenityNames(amenityNames);
        }
    }

    private int getCapacityByRoomType(String roomTypeName) {
        // Customize this mapping based on your app's logic
        switch (roomTypeName.toLowerCase()) {
            case "single":
                return 1;
            case "double":
                return 2;
            case "family":
                return 4;
            case "suite":
                return 3;
            default:
                return 1; // fallback
        }
    }

}
