package com.hotel.booking.controller;

import com.hotel.booking.model.Hotel;
import com.hotel.booking.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelRepository hotelRepo;

    @GetMapping
    public List<Hotel> list(@RequestParam(required = false) String city) {
        return city == null ? hotelRepo.findAll() : hotelRepo.findByCityIgnoreCase(city);
    }

    @GetMapping("/{id}")
    public Hotel get(@PathVariable Integer id) {
        return hotelRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found"));
    }

    // Admin only (protect this once you add security)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Hotel create(@RequestBody Hotel hotel) {
        return hotelRepo.save(hotel);
    }
}
