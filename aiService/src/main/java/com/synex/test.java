package com.synex;

import com.synex.service.HotelEmbeddingService;

public class test {

	public static void main(String[] args) {
	    HotelEmbeddingService s = new HotelEmbeddingService(null); // inject JdbcTemplate or mock it
	    s.embedAndSaveHotel(1, "Simple test description");
	}

}
