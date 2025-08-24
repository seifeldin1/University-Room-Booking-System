package com.example.University.controller;

import com.example.University.dto.BookingRequestDTO;
import com.example.University.dto.BookingResponseDTO;
import com.example.University.service.BookingCancellationService;
import com.example.University.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final BookingCancellationService bookingCancellationService;

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

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'FACULTY', 'ADMIN')")
    public ResponseEntity<BookingResponseDTO> cancelBooking(
            @PathVariable Long id,
            Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return ResponseEntity.badRequest().build();
        }

        BookingResponseDTO cancelledBooking = bookingCancellationService.cancelBooking(id, authentication.getName());

        return ResponseEntity.ok(cancelledBooking);
    }

    @DeleteMapping("/{id}/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookingResponseDTO> cancelBookingByAdmin(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            Authentication authentication) {
        BookingResponseDTO cancelledBooking = bookingCancellationService.cancelBookingByAdmin(
                id, authentication.getName(), reason);
        return ResponseEntity.ok(cancelledBooking);
    }
}
