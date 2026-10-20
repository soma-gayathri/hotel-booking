package com.hotel.booking.service;

import com.hotel.booking.dto.BookingRequest;
import com.hotel.booking.model.*;
import com.hotel.booking.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepo;
    private final RoomRepository roomRepo;
    private final UserRepository userRepo;

    @Transactional
    public Booking create(BookingRequest req) {
        if (!req.checkOut().isAfter(req.checkIn())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Check-out must be after check-in");
        }
        User user = userRepo.findById(req.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Room room = roomRepo.findById(req.roomId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));

        if (req.guests() > room.getMaxGuests()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Room allows max " + room.getMaxGuests() + " guests");
        }
        if (bookingRepo.countOverlapping(room.getRoomId(), req.checkIn(), req.checkOut()) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Room is not available for these dates");
        }

        long nights = ChronoUnit.DAYS.between(req.checkIn(), req.checkOut());

        Booking b = new Booking();
        b.setUser(user);
        b.setRoom(room);
        b.setCheckIn(req.checkIn());
        b.setCheckOut(req.checkOut());
        b.setGuests(req.guests());
        b.setTotalPrice(room.getPricePerNight().multiply(BigDecimal.valueOf(nights)));

        try {
            // flush now so the DB's anti-overlap constraint fires here (catches race conditions)
            return bookingRepo.saveAndFlush(b);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Room was just booked by someone else");
        }
    }

    public List<Booking> forUser(Integer userId) {
        return bookingRepo.findByUserUserIdOrderByCheckInDesc(userId);
    }

    @Transactional
    public Booking cancel(Integer bookingId) {
        Booking b = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        if ("cancelled".equals(b.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Already cancelled");
        }
        b.setStatus("cancelled");
        return bookingRepo.save(b);
    }
}
