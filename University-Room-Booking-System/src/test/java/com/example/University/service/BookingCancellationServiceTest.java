package com.example.University.service;

import com.example.University.dto.BookingResponseDTO;
import com.example.University.entity.Booking;
import com.example.University.entity.Room;
import com.example.University.entity.User;
import com.example.University.entity.Booking.BookingStatus;
import com.example.University.exception.ResourceNotFoundException;
import com.example.University.exception.UnauthorizedActionException;
import com.example.University.repository.BookingRepository;
import com.example.University.repository.BookingHistoryRepository;
import com.example.University.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingCancellationServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingHistoryRepository bookingHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookingCancellationService cancellationService;

    private User testUser;
    private User otherUser;
    private Room testRoom;
    private Booking testBooking;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("student@university.edu");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");

        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setEmail("other@university.edu");

        testRoom = new Room();
        testRoom.setId(1L);
        testRoom.setName("Test Room");

        testBooking = new Booking();
        testBooking.setId(1L);
        testBooking.setRoom(testRoom);
        testBooking.setUser(testUser);
        testBooking.setStartTime(LocalDateTime.now().plusDays(2));
        testBooking.setEndTime(LocalDateTime.now().plusDays(2).plusHours(2));
        testBooking.setStatus(BookingStatus.PENDING);
        testBooking.setPurpose("Test booking");
    }

    @Test
    void testCancelBooking_Success() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(userRepository.findByEmail("student@university.edu")).thenReturn(Optional.of(testUser));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

        BookingResponseDTO result = cancellationService.cancelBooking(1L, "student@university.edu");

        assertNotNull(result);
        assertEquals(BookingStatus.CANCELLED.name(), result.getStatus());
        verify(bookingRepository).save(any(Booking.class));
        verify(bookingHistoryRepository).save(any());
    }

    @Test
    void testCancelBooking_UnauthorizedUser_ThrowsException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(userRepository.findByEmail("other@university.edu")).thenReturn(Optional.of(otherUser));

        assertThrows(UnauthorizedActionException.class, () ->
                cancellationService.cancelBooking(1L, "other@university.edu"));
    }

    @Test
    void testCancelBooking_BookingNotFound_ThrowsException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                cancellationService.cancelBooking(1L, "student@university.edu"));
    }

    @Test
    void testCancelBooking_AlreadyCancelled_ThrowsException() {
        testBooking.setStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(userRepository.findByEmail("student@university.edu")).thenReturn(Optional.of(testUser));

        assertThrows(IllegalStateException.class, () ->
                cancellationService.cancelBooking(1L, "student@university.edu"));
    }

    @Test
    void testCancelBooking_PastStartTime_ThrowsException() {
        testBooking.setStartTime(LocalDateTime.now().minusHours(1));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(userRepository.findByEmail("student@university.edu")).thenReturn(Optional.of(testUser));

        assertThrows(IllegalStateException.class, () ->
                cancellationService.cancelBooking(1L, "student@university.edu"));
    }

    @Test
    void testCancelBooking_WithinOneHour_ThrowsException() {
        testBooking.setStartTime(LocalDateTime.now().plusMinutes(30));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(userRepository.findByEmail("student@university.edu")).thenReturn(Optional.of(testUser));

        assertThrows(IllegalStateException.class, () ->
                cancellationService.cancelBooking(1L, "student@university.edu"));
    }

    @Test
    void testCancelBookingByAdmin_Success() {
        User admin = new User();
        admin.setId(3L);
        admin.setEmail("admin@university.edu");
        admin.setFirstName("Admin");
        admin.setLastName("User");

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(userRepository.findByEmail("admin@university.edu")).thenReturn(Optional.of(admin));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

        BookingResponseDTO result = cancellationService.cancelBookingByAdmin(1L, "admin@university.edu", "Administrative decision");

        assertNotNull(result);
        assertEquals(BookingStatus.CANCELLED.name(), result.getStatus());
        verify(bookingRepository).save(any(Booking.class));
        verify(bookingHistoryRepository).save(any());
    }

    @Test
    void testCancelBookingByAdmin_AlreadyCancelled_ThrowsException() {
        testBooking.setStatus(BookingStatus.CANCELLED);
        User admin = new User();
        admin.setEmail("admin@university.edu");

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(userRepository.findByEmail("admin@university.edu")).thenReturn(Optional.of(admin));

        assertThrows(IllegalStateException.class, () ->
                cancellationService.cancelBookingByAdmin(1L, "admin@university.edu", "test"));
    }
}