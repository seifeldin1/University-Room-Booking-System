package com.example.University.service.impl;

import com.example.University.dto.BookingResponseDTO;
import com.example.University.entity.Booking;
import com.example.University.entity.BookingHistory;
import com.example.University.entity.User;
import com.example.University.entity.Booking.BookingStatus;
import com.example.University.exception.ResourceNotFoundException;
import com.example.University.exception.UnauthorizedActionException;
import com.example.University.repository.BookingRepository;
import com.example.University.repository.BookingHistoryRepository;
import com.example.University.repository.UserRepository;
import com.example.University.service.BookingCancellationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingCancellationServiceImpl implements BookingCancellationService {

    private final BookingRepository bookingRepository;
    private final BookingHistoryRepository bookingHistoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BookingResponseDTO cancelBooking(Long bookingId, String userEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedActionException("You can only cancel your own bookings");
        }

        validateCancellationRules(booking);

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancelledBy(user);

        Booking cancelledBooking = bookingRepository.save(booking);

        recordCancellationHistory(booking, user, "Booking cancelled by requester");

        return mapToResponseDto(cancelledBooking);
    }

    @Override
    @Transactional
    public BookingResponseDTO cancelBookingByAdmin(Long bookingId, String adminEmail, String reason) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found: " + adminEmail));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancelledBy(admin);

        Booking cancelledBooking = bookingRepository.save(booking);

        String historyReason = reason != null && !reason.trim().isEmpty()
                ? "Booking cancelled by admin: " + reason
                : "Booking cancelled by admin";

        recordCancellationHistory(booking, admin, historyReason);

        return mapToResponseDto(cancelledBooking);
    }


    private void validateCancellationRules(Booking booking) {
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled");
        }

        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.APPROVED) {
            throw new IllegalStateException("Only pending or approved bookings can be cancelled");
        }

        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(booking.getStartTime()) || now.isEqual(booking.getStartTime())) {
            throw new IllegalStateException("Cannot cancel booking that has already started or is starting now");
        }

        LocalDateTime minimumCancellationTime = booking.getStartTime().minusHours(1);

        if (now.isAfter(minimumCancellationTime)) {
            throw new IllegalStateException("Cannot cancel booking within 1 hour of start time");
        }
    }

    private void recordCancellationHistory(Booking booking, User actionBy, String reason) {
        BookingHistory history = new BookingHistory();
        history.setBooking(booking);
        history.setAction(BookingHistory.BookingHistoryAction.CANCELLED);
        history.setActionBy(actionBy);
        history.setActionAt(LocalDateTime.now());
        history.setReason(reason);
        history.setOldStatus(booking.getStatus());
        history.setNewStatus(BookingStatus.CANCELLED);
        bookingHistoryRepository.save(history);
    }

    private BookingResponseDTO mapToResponseDto(Booking booking) {
        BookingResponseDTO dto = new BookingResponseDTO();
        dto.setId(booking.getId());
        dto.setRoomId(booking.getRoom().getId());
        dto.setUserId(booking.getUser().getId());
        dto.setStartTime(booking.getStartTime());
        dto.setEndTime(booking.getEndTime());
        dto.setPurpose(booking.getPurpose());
        dto.setStatus(booking.getStatus().name());

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            dto.setCancelledAt(booking.getCancelledAt());
            if (booking.getCancelledBy() != null) {
                dto.setCancelledByName(
                        booking.getCancelledBy().getFirstName() + " " + booking.getCancelledBy().getLastName()
                );
            }
        }

        return dto;
    }
}
