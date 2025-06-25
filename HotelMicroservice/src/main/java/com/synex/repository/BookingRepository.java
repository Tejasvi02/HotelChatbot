package com.synex.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.synex.domain.Booking;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    
}