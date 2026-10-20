package com.hotel.booking.repository;

import com.hotel.booking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {

    List<Booking> findByUserUserIdOrderByCheckInDesc(Integer userId);

    @Query("""
        select count(b) from Booking b
        where b.room.roomId = :roomId
          and b.status <> 'cancelled'
          and b.checkIn < :checkOut
          and b.checkOut > :checkIn
        """)
    long countOverlapping(@Param("roomId") Integer roomId,
                          @Param("checkIn") LocalDate checkIn,
                          @Param("checkOut") LocalDate checkOut);
}
