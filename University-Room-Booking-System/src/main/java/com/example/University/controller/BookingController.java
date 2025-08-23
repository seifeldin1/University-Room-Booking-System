package com.example.University.controller;

import com.example.University.dto.BookingRequestDTO;
import com.example.University.dto.BookingResponseDTO;
import com.example.University.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // GET availability of a room
    @GetMapping("/{id}/availability")
    public ResponseEntity<List<BookingService.TimeSlot>> getAvailability(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        if (end.isBefore(start)) {
            return ResponseEntity.badRequest().build();
        }

        List<BookingService.TimeSlot> freeSlots = bookingService.getFreeSlots(id, start, end);
        return ResponseEntity.ok(freeSlots);
    }

    // POST create booking request
    @PostMapping
    public ResponseEntity<BookingResponseDTO> createBooking(
            @RequestBody BookingRequestDTO request,
            @RequestParam Long userId
    ) {
        BookingResponseDTO booking = bookingService.createBooking(request, userId);
        // 201 Created with Location header
        return ResponseEntity.created(URI.create("/api/booking/" + booking.getId()))
                .body(booking);
    }
}
