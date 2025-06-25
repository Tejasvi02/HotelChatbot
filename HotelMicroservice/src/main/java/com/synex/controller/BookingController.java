package com.synex.controller;

import com.synex.domain.Booking;
import com.synex.domain.Hotel;
import com.synex.repository.BookingRepository;
import com.synex.repository.HotelRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest request) {
        Hotel hotel = null;

        if (request.getHotelId() != 0) {
            Optional<Hotel> optHotel = hotelRepository.findById(request.getHotelId());
            if (optHotel.isPresent()) {
                hotel = optHotel.get();
            }
        }

        if (hotel == null && request.getHotelName() != null && !request.getHotelName().isEmpty()) {
            hotel = hotelRepository.findByHotelNameIgnoreCase(request.getHotelName());
        }

        if (hotel == null) {
            return ResponseEntity.badRequest().body("Hotel not found: " + request.getHotelName());
        }

        Booking booking = new Booking();
        booking.setHotel(hotel);
        booking.setNumRooms(request.getNumRooms());
        booking.setNumGuests(request.getNumGuests());
        booking.setCheckInDate(LocalDate.parse(request.getCheckInDate()));
        booking.setCheckOutDate(LocalDate.parse(request.getCheckOutDate()));
        booking.setUsername(request.getUsername());
        booking.setDateBooked(LocalDate.now());
        booking.setStatus("Booked");
        booking.setTotalPrice(booking.getNumRooms()*(hotel.getAveragePrice()-hotel.getDiscount()));

        bookingRepository.save(booking);

        return ResponseEntity.ok("Booking successful! for hotel "+ hotel.getHotelName()+" with ID: " + booking.getBookingId()+
        		" for dates from "+booking.getCheckInDate()+" to "+ booking.getCheckOutDate());
    }

    // DTO for booking request in Hotel microservice (matches AI service DTO)
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
