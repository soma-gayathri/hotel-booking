package com.hotel.booking.repository;

import com.hotel.booking.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel, Integer> {
    List<Hotel> findByCityIgnoreCase(String city);
}
