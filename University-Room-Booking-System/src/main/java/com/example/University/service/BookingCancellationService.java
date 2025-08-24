package com.example.University.service;

import com.example.University.dto.BookingResponseDTO;

public interface BookingCancellationService {

    BookingResponseDTO cancelBooking(Long bookingId, String userEmail);

    BookingResponseDTO cancelBookingByAdmin(Long bookingId, String adminEmail, String reason);

}
