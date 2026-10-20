package com.hotel.booking.controller;

import com.hotel.booking.model.Room;
import com.hotel.booking.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomRepository roomRepo;

    // GET /api/rooms/available?city=Tirupati&checkIn=2026-11-01&checkOut=2026-11-05&guests=2
    @GetMapping("/available")
    public List<Room> available(@RequestParam String city,
                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
                                @RequestParam(defaultValue = "1") int guests) {
        if (!checkOut.isAfter(checkIn)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Check-out must be after check-in");
        }
        return roomRepo.findAvailable(city, checkIn, checkOut, guests);
    }

    // Admin only (protect this once you add security)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Room create(@RequestBody Room room) {
        return roomRepo.save(room);
    }
}
