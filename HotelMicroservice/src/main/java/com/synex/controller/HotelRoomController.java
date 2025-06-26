package com.synex.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.synex.domain.HotelRoom;
import com.synex.repository.HotelRoomRepository;

@RestController
@RequestMapping("/hotel-rooms")
public class HotelRoomController {

 @Autowired
 private HotelRoomRepository hotelRoomRepository;

 @GetMapping
 public HotelRoom getRoomByHotelIdAndType(
     @RequestParam Integer hotelId,
     @RequestParam Integer typeId
 ) {
     return hotelRoomRepository.findFirstByHotelHotelId(hotelId);
 }
}
