package com.hotel.booking.repository;

import com.hotel.booking.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Integer> {

    // Rooms in a city that have no overlapping, non-cancelled booking
    @Query("""
        select r from Room r
        where lower(r.hotel.city) = lower(:city)
          and r.maxGuests >= :guests
          and not exists (
              select b.bookingId from Booking b
              where b.room = r
                and b.status <> 'cancelled'
                and b.checkIn < :checkOut
                and b.checkOut > :checkIn)
        order by r.pricePerNight
        """)
    List<Room> findAvailable(@Param("city") String city,
                             @Param("checkIn") LocalDate checkIn,
                             @Param("checkOut") LocalDate checkOut,
                             @Param("guests") int guests);
}
