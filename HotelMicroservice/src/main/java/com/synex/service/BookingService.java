package com.synex.service;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.synex.domain.Booking;
import com.synex.domain.Hotel;
import com.synex.domain.HotelRoom;
import com.synex.repository.BookingRepository;
import com.synex.repository.HotelRepository;
import com.synex.repository.HotelRoomRepository;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepo;

    @Autowired
    private HotelRepository hotelRepo;

    @Autowired
    private HotelRoomRepository hotelRoomRepo;

    public Booking createBooking(int hotelId, int roomId, int numRooms, int numGuests,
                                 LocalDate checkIn, LocalDate checkOut, String username) throws Exception {

        Hotel hotel = hotelRepo.findById(hotelId).orElseThrow(() -> new Exception("Hotel not found"));
        HotelRoom room = hotelRoomRepo.findById(roomId).orElseThrow(() -> new Exception("Room not found"));

        if (room.getHotel() == null || room.getHotel().getHotelId() != hotelId) {
            throw new Exception("Room does not belong to hotel");
        }

        double price = room.getPrice() * numRooms * (checkOut.toEpochDay() - checkIn.toEpochDay());

        Booking booking = new Booking();
        booking.setHotel(hotel);
        booking.setHotelRoom(room);
        booking.setNumGuests(numGuests);
        booking.setNumRooms(numRooms);
        booking.setCheckInDate(checkIn);
        booking.setCheckOutDate(checkOut);
        booking.setUsername(username);
        booking.setTotalPrice(price);
        booking.setStatus("BOOKED");
        booking.setDateBooked(LocalDate.now());

        return bookingRepo.save(booking);
    }
}
