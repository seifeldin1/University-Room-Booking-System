package com.example.University.controller;

import com.example.University.dto.BookingRequestDTO;
import com.example.University.dto.BookingResponseDTO;
import com.example.University.dto.BookingHistoryResponseDTO;
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
    // ✅ REMOVED: BookingCancellationService dependency (Person 5's responsibility)

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
    @PreAuthorize("hasAnyRole('STUDENT', 'FACULTY')")
    public ResponseEntity<BookingResponseDTO> createBooking(
            @RequestBody BookingRequestDTO request,
            @RequestParam Long userId
    ) {
        BookingResponseDTO booking = bookingService.createBooking(request, userId);
        // 201 Created with Location header
        return ResponseEntity.created(URI.create("/api/booking/" + booking.getId()))
                .body(booking);
    }

    // ===== PERSON 4's ENDPOINTS: APPROVAL & HISTORY =====

    // PATCH approve booking (Admin only)
    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookingResponseDTO> approveBooking(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            Authentication authentication
    ) {
        BookingResponseDTO approvedBooking = bookingService.approveBooking(id, authentication.getName(), reason);
        return ResponseEntity.ok(approvedBooking);
    }

    // PATCH reject booking (Admin only)
    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookingResponseDTO> rejectBooking(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            Authentication authentication
    ) {
        BookingResponseDTO rejectedBooking = bookingService.rejectBooking(id, authentication.getName(), reason);
        return ResponseEntity.ok(rejectedBooking);
    }

    // GET booking history (Audit trail)
    @GetMapping("/{id}/history")
    @PreAuthorize("hasAnyRole('STUDENT', 'FACULTY', 'ADMIN')")
    public ResponseEntity<List<BookingHistoryResponseDTO>> getBookingHistory(
            @PathVariable Long id
    ) {
        List<BookingHistoryResponseDTO> history = bookingService.getBookingHistory(id);
        return ResponseEntity.ok(history);
    }

    // ===== PERSON 5's CANCELLATION ENDPOINTS (COMMENTED OUT UNTIL IMPLEMENTED) =====
    /*
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
    */
}