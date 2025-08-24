package com.example.University.service;

import com.example.University.dto.BookingRequestDTO;
import com.example.University.dto.BookingResponseDTO;
import com.example.University.entity.Booking;
import com.example.University.entity.Role;
import com.example.University.entity.Room;
import com.example.University.entity.User;
import com.example.University.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingHistoryRepository bookingHistoryRepository;
    @Mock
    private HolidayRepository holidayRepository;
    @Mock
    private HolidayService holidayService;

    @InjectMocks
    private BookingService bookingService;

    private Room room;
    private User studentUser;
    private BookingRequestDTO request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        room = Room.builder().id(1L).build();

        studentUser = User.builder()
                .id(10L)
                .roles(Set.of(Role.builder().name(Role.RoleName.STUDENT).build()))
                .build();

        request = BookingRequestDTO.builder()
                .roomId(room.getId())
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .purpose("Study Group")
                .build();
    }

    @Test
    void createBooking_success() {
        // Arrange
        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        when(userRepository.findById(studentUser.getId())).thenReturn(Optional.of(studentUser));
        when(bookingRepository.existsActiveOverlap(anyLong(), any(), any())).thenReturn(false);
        when(holidayService.existsByDate(any())).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(99L);
            return b;
        });

        // Act
        BookingResponseDTO response = bookingService.createBooking(request, studentUser.getId());

        // Assert
        assertNotNull(response);
        assertEquals(99L, response.getId());
        assertEquals(room.getId(), response.getRoomId());
        assertEquals(studentUser.getId(), response.getUserId());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void createBooking_roomNotFound() {
        when(roomRepository.findById(room.getId())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> bookingService.createBooking(request, studentUser.getId()));
    }

    @Test
    void createBooking_userNotFound() {
        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        when(userRepository.findById(studentUser.getId())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> bookingService.createBooking(request, studentUser.getId()));
    }

    @Test
    void createBooking_invalidRole() {
        User adminUser = User.builder()
                .id(20L)
                .roles(Set.of(Role.builder().name(Role.RoleName.ADMIN).build()))
                .build();

        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.createBooking(request, adminUser.getId()));
    }

    @Test
    void createBooking_endBeforeStart() {
        request.setEndTime(request.getStartTime().minusHours(1));

        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        when(userRepository.findById(studentUser.getId())).thenReturn(Optional.of(studentUser));

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.createBooking(request, studentUser.getId()));
    }

    @Test
    void createBooking_conflict() {
        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        when(userRepository.findById(studentUser.getId())).thenReturn(Optional.of(studentUser));
        when(bookingRepository.existsActiveOverlap(anyLong(), any(), any())).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.createBooking(request, studentUser.getId()));
    }

    @Test
    void createBooking_holidayRestriction() {
        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        when(userRepository.findById(studentUser.getId())).thenReturn(Optional.of(studentUser));
        when(bookingRepository.existsActiveOverlap(anyLong(), any(), any())).thenReturn(false);
        when(holidayService.existsByDate(any())).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> bookingService.createBooking(request, studentUser.getId()));
    }
}

