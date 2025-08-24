package com.example.University.service;

import com.example.University.dto.BookingRequestDTO;
import com.example.University.dto.BookingResponseDTO;
import com.example.University.dto.BookingHistoryResponseDTO;
import com.example.University.entity.Booking;
import com.example.University.entity.BookingHistory;
import com.example.University.entity.Role;
import com.example.University.entity.Room;
import com.example.University.entity.User;
import com.example.University.repository.*;
import com.example.University.exception.ResourceNotFoundException;
import com.example.University.exception.UnauthorizedActionException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final BookingHistoryRepository bookingHistoryRepository;
    private final HolidayRepository holidayRepository;
    private final HolidayService holidayService;

    public List<TimeSlot> getFreeSlots(Long roomId, LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        // Step 1: get all bookings in range
        List<Booking> bookings = bookingRepository.findActiveBookingsInRange(roomId, rangeStart, rangeEnd);
        List<TimeSlot> freeSlots = new ArrayList<TimeSlot>();

        // Step 2: walk through timeline
        LocalDateTime current = rangeStart;
        for (Booking booking : bookings) {
            if (current.isBefore(booking.getStartTime())) {
                freeSlots.add(new TimeSlot(current, booking.getStartTime()));
            }
            current = booking.getEndTime().isAfter(current) ? booking.getEndTime() : current;
        }

        // Step 3: add last gap until rangeEnd
        if (current.isBefore(rangeEnd)) {
            freeSlots.add(new TimeSlot(current, rangeEnd));
        }

        return freeSlots;
    }

    @Transactional
    public BookingResponseDTO createBooking(BookingRequestDTO dto, Long userId) {
        // 1. Validate room
        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));

        // 2. Validate user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // 3. Role validation -> must be STUDENT or FACULTY
        boolean hasAccess = user.getRoles().stream()
                .anyMatch(role -> role.getName() == Role.RoleName.STUDENT
                        || role.getName() == Role.RoleName.FACULTY);

        if (!hasAccess) {
            throw new IllegalArgumentException("Only students or faculty can create bookings");
        }

        // 4. Time validation
        if (dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        // 5. Overlap validation
        boolean conflict = bookingRepository.existsActiveOverlap(
                room.getId(), dto.getStartTime(), dto.getEndTime()
        );
        if (conflict) {
            throw new IllegalArgumentException("Booking overlaps with an existing booking");
        }
        validateHolidayRestrictions(dto.getStartTime(), dto.getEndTime());

        // 6. Create booking (status = PENDING)
        Booking booking = Booking.builder()
                .room(room)
                .user(user)
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .purpose(dto.getPurpose())
                .status(Booking.BookingStatus.PENDING)
                .build();

        booking = bookingRepository.save(booking);

        // 7. Log creation in history
        logBookingHistory(booking, user, BookingHistory.BookingHistoryAction.CREATED,
                null, Booking.BookingStatus.PENDING);

        Long cancelledById = null;
        String cancelledByName = null;
        if (booking.getCancelledBy() != null) {
            cancelledById = booking.getCancelledBy().getId();
            cancelledByName = booking.getCancelledBy().getFirstName();
        }

        // 8. Return DTO
        return new BookingResponseDTO(
                booking.getId(),
                booking.getRoom().getId(),
                booking.getUser().getId(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getPurpose(),
                booking.getStatus().name(),
                booking.getCancelledAt(),
                cancelledById,
                cancelledByName
        );
    }

    // ===== PERSON 4's MAIN RESPONSIBILITY: APPROVAL LOGIC =====

    @Transactional
    public BookingResponseDTO approveBooking(Long bookingId, String adminUsername, String reason) {
        // 1. Find booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // 2. Find admin user by username
        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));

        // 3. Validate admin role
        boolean isAdmin = admin.getRoles().stream()
                .anyMatch(role -> role.getName() == Role.RoleName.ADMIN);
        if (!isAdmin) {
            throw new UnauthorizedActionException("Only admins can approve bookings");
        }

        // 4. Can only approve PENDING bookings
        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            throw new IllegalStateException("Can only approve PENDING bookings. Current status: " + booking.getStatus());
        }

        // 5. RE-CHECK for overlaps (someone might have booked in the meantime)
        boolean conflict = bookingRepository.existsActiveOverlap(
                booking.getRoom().getId(),
                booking.getStartTime(),
                booking.getEndTime()
        );
        if (conflict) {
            // Check if the conflict is with this same booking
            List<Booking> conflictingBookings = bookingRepository.findActiveBookingsInRange(
                    booking.getRoom().getId(),
                    booking.getStartTime(),
                    booking.getEndTime()
            );
            Booking finalBooking = booking;
            boolean hasRealConflict = conflictingBookings.stream()
                    .anyMatch(b -> !b.getId().equals(finalBooking.getId()));

            if (hasRealConflict) {
                throw new IllegalStateException("Cannot approve: booking now overlaps with another approved booking");
            }
        }

        // 6. Update status
        booking.setStatus(Booking.BookingStatus.APPROVED);
        booking = bookingRepository.save(booking);

        // 7. Log history
        logBookingHistory(booking, admin, BookingHistory.BookingHistoryAction.APPROVED,
                reason, Booking.BookingStatus.APPROVED);

        return mapToBookingResponseDTO(booking);
    }

    @Transactional
    public BookingResponseDTO rejectBooking(Long bookingId, String adminUsername, String reason) {
        // 1. Find booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // 2. Find admin user by username
        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));

        // 3. Validate admin role
        boolean isAdmin = admin.getRoles().stream()
                .anyMatch(role -> role.getName() == Role.RoleName.ADMIN);
        if (!isAdmin) {
            throw new UnauthorizedActionException("Only admins can reject bookings");
        }

        // 4. Can only reject PENDING bookings
        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            throw new IllegalStateException("Can only reject PENDING bookings. Current status: " + booking.getStatus());
        }

        // 5. Update status
        booking.setStatus(Booking.BookingStatus.REJECTED);
        booking = bookingRepository.save(booking);

        // 6. Log history
        logBookingHistory(booking, admin, BookingHistory.BookingHistoryAction.REJECTED,
                reason, Booking.BookingStatus.REJECTED);

        return mapToBookingResponseDTO(booking);
    }

    // ===== PERSON 4's SECOND RESPONSIBILITY: BOOKING HISTORY =====

    public List<BookingHistoryResponseDTO> getBookingHistory(Long bookingId) {
        // Verify booking exists
        if (!bookingRepository.existsById(bookingId)) {
            throw new ResourceNotFoundException("Booking not found");
        }

        List<BookingHistory> history = bookingHistoryRepository.findByBookingIdOrderByActionAtDesc(bookingId);

        return history.stream()
                .map(this::mapToBookingHistoryDTO)
                .collect(Collectors.toList());
    }

    // ===== HELPER METHODS =====

    // SIMPLIFIED: No previousStatus tracking (until BookingHistory entity is updated)
    private void logBookingHistory(Booking booking, User actionBy, BookingHistory.BookingHistoryAction action,
                                   String reason, Booking.BookingStatus newStatus) {
        BookingHistory history = BookingHistory.builder()
                .booking(booking)
                .action(action)
                .actionBy(actionBy)
                .reason(reason)
                .newStatus(newStatus)
                .build();

        bookingHistoryRepository.save(history);
    }

    private BookingResponseDTO mapToBookingResponseDTO(Booking booking) {
        Long cancelledById = null;
        String cancelledByName = null;
        if (booking.getCancelledBy() != null) {
            cancelledById = booking.getCancelledBy().getId();
            cancelledByName = booking.getCancelledBy().getFirstName();
        }

        return new BookingResponseDTO(
                booking.getId(),
                booking.getRoom().getId(),
                booking.getUser().getId(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getPurpose(),
                booking.getStatus().name(),
                booking.getCancelledAt(),
                cancelledById,
                cancelledByName
        );
    }

    // SIMPLIFIED: Return null for previousStatus until entity is fixed
    private BookingHistoryResponseDTO mapToBookingHistoryDTO(BookingHistory history) {
        return new BookingHistoryResponseDTO(
                history.getId(),
                history.getBooking().getId(),
                history.getAction().name(),
                history.getActionBy().getId(),
                history.getActionBy().getFirstName() + " " + history.getActionBy().getLastName(),
                history.getActionAt(),
                history.getReason(),
                null, // previousStatus - not available until entity is fixed
                history.getNewStatus() != null ? history.getNewStatus().name() : null
        );
    }

    private void validateHolidayRestrictions(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDate startDate = startTime.toLocalDate();
        LocalDate endDate = endTime.toLocalDate();
        if (holidayService.existsByDate(startDate)) {
            throw new IllegalStateException("Booking cannot be made on a holiday: " + startDate);
        }
        if (!startDate.equals(endDate)) {
            if (holidayService.existsByDateBetween(startDate, endDate)) {
                throw new IllegalStateException("Booking cannot span across holidays between " + startDate + " and " + endDate);
            }
        }
    }

    @Getter
    @AllArgsConstructor
    public static class TimeSlot {
        private LocalDateTime start;
        private LocalDateTime end;
    }
}