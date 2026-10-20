package com.hotel.booking.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record BookingRequest(
        @NotNull Integer userId,
        @NotNull Integer roomId,
        @NotNull @FutureOrPresent LocalDate checkIn,
        @NotNull @Future LocalDate checkOut,
        @NotNull @Min(1) Integer guests) {}
