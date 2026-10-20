package com.hotel.booking.controller;

import com.hotel.booking.dto.BookingRequest;
import com.hotel.booking.model.Booking;
import com.hotel.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Booking create(@Valid @RequestBody BookingRequest req) {
        return service.create(req);
    }

    @GetMapping("/user/{userId}")
    public List<Booking> myBookings(@PathVariable Integer userId) {
        return service.forUser(userId);
    }

    @PutMapping("/{id}/cancel")
    public Booking cancel(@PathVariable Integer id) {
        return service.cancel(id);
    }
}
