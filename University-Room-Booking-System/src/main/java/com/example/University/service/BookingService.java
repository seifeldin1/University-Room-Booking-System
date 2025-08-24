package com.example.University.service;

import com.example.University.dto.BookingRequestDTO;
import com.example.University.dto.BookingResponseDTO;
import com.example.University.entity.Booking;
import com.example.University.entity.Role;
import com.example.University.entity.Room;
import com.example.University.entity.User;
import com.example.University.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.AssertFalse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

        Long cancelledById = null;
        String cancelledByName = null;
        if (booking.getCancelledBy() != null) {
            cancelledById = booking.getCancelledBy().getId();
            cancelledByName = booking.getCancelledBy().getFirstName();
        }

        // 7. Return DTO
        return new BookingResponseDTO(
                booking.getId(),
                room.getId(),
                user.getId(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getPurpose(),
                booking.getStatus().name(),
                booking.getCancelledAt(),
                cancelledById,
                cancelledByName
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
