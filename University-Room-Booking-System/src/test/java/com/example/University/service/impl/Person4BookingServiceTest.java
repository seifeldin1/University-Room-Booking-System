package com.example.University.service.impl;

import com.example.University.dto.BookingHistoryResponseDTO;
import com.example.University.dto.BookingResponseDTO;
import com.example.University.entity.Booking;
import com.example.University.entity.BookingHistory;
import com.example.University.entity.Role;
import com.example.University.entity.Room;
import com.example.University.entity.User;
import com.example.University.exception.ResourceNotFoundException;
import com.example.University.exception.UnauthorizedActionException;
import com.example.University.repository.BookingHistoryRepository;
import com.example.University.repository.BookingRepository;
import com.example.University.repository.UserRepository;
import com.example.University.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Person4BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingHistoryRepository bookingHistoryRepository;

    @InjectMocks
    private BookingService bookingService;

    private User adminUser;
    private User regularUser;
    private Room testRoom;
    private Role adminRole;
    private Role userRole;
    private Booking pendingBooking;
    private Booking approvedBooking;
    private BookingHistory historyRecord;

    @BeforeEach
    void setUp() {
        // Setup roles
        adminRole = Role.builder()
                .id(1L)
                .name(Role.RoleName.ADMIN)
                .description("Administrator role")
                .build();

        userRole = Role.builder()
                .id(2L)
                .name(Role.RoleName.STUDENT)
                .description("Student role")
                .build();

        // Setup admin user
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(adminRole);

        adminUser = User.builder()
                .id(1L)
                .username("admin")
                .email("admin@university.com")
                .firstName("Admin")
                .lastName("User")
                .roles(adminRoles)
                .isActive(true)
                .build();

        // Setup regular user
        Set<Role> studentRoles = new HashSet<>();
        studentRoles.add(userRole);

        regularUser = User.builder()
                .id(2L)
                .username("student")
                .email("student@university.com")
                .firstName("John")
                .lastName("Doe")
                .roles(studentRoles)
                .isActive(true)
                .build();

        // Setup test room
        testRoom = Room.builder()
                .id(1L)
                .name("Conference Room A")
                .capacity(20)
                .build();

        // Setup pending booking
        pendingBooking = Booking.builder()
                .id(1L)
                .user(regularUser)
                .room(testRoom)
                .status(Booking.BookingStatus.PENDING)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .purpose("Team Meeting")
                .createdAt(LocalDateTime.now())
                .build();

        // Setup approved booking
        approvedBooking = Booking.builder()
                .id(2L)
                .user(regularUser)
                .room(testRoom)
                .status(Booking.BookingStatus.APPROVED)
                .startTime(LocalDateTime.now().plusDays(2))
                .endTime(LocalDateTime.now().plusDays(2).plusHours(2))
                .purpose("Another Meeting")
                .createdAt(LocalDateTime.now())
                .build();

        // Setup booking history
        historyRecord = BookingHistory.builder()
                .id(1L)
                .booking(pendingBooking)
                .action(BookingHistory.BookingHistoryAction.CREATED)
                .actionBy(regularUser)
                .reason("Initial booking creation")
                .newStatus(Booking.BookingStatus.PENDING)
                .actionAt(LocalDateTime.now())
                .build();
    }

    // ================== APPROVAL TESTS ==================

    @Test
    void approveBooking_Success_WithAdminUser() {
        // Arrange
        String reason = "Meeting approved for business requirements";

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(pendingBooking));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(bookingRepository.existsActiveOverlap(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(bookingRepository.findActiveBookingsInRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList()); // No conflicts
        when(bookingRepository.save(any(Booking.class))).thenReturn(pendingBooking);

        // Act
        BookingResponseDTO result = bookingService.approveBooking(1L, "admin", reason);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("APPROVED", result.getStatus());
        assertEquals(Booking.BookingStatus.APPROVED, pendingBooking.getStatus());

        verify(bookingRepository).findById(1L);
        verify(userRepository).findByUsername("admin");
        verify(bookingRepository).save(pendingBooking);
        verify(bookingHistoryRepository).save(any(BookingHistory.class));
    }

    @Test
    void approveBooking_ThrowsException_WhenBookingNotFound() {
        // Arrange
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> bookingService.approveBooking(999L, "admin", "Test reason"));

        assertEquals("Booking not found", exception.getMessage());
        verify(bookingRepository).findById(999L);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void approveBooking_ThrowsException_WhenAdminNotFound() {
        // Arrange
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(pendingBooking));
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> bookingService.approveBooking(1L, "nonexistent", "Test reason"));

        assertEquals("Admin user not found", exception.getMessage());
        verify(bookingRepository).findById(1L);
        verify(userRepository).findByUsername("nonexistent");
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void approveBooking_ThrowsException_WhenUserNotAdmin() {
        // Arrange
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(pendingBooking));
        when(userRepository.findByUsername("student")).thenReturn(Optional.of(regularUser));

        // Act & Assert
        UnauthorizedActionException exception = assertThrows(UnauthorizedActionException.class,
                () -> bookingService.approveBooking(1L, "student", "Test reason"));

        assertEquals("Only admins can approve bookings", exception.getMessage());
        verify(bookingRepository).findById(1L);
        verify(userRepository).findByUsername("student");
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void approveBooking_ThrowsException_WhenBookingNotPending() {
        // Arrange
        when(bookingRepository.findById(2L)).thenReturn(Optional.of(approvedBooking));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> bookingService.approveBooking(2L, "admin", "Test reason"));

        assertEquals("Can only approve PENDING bookings. Current status: APPROVED", exception.getMessage());
        verify(bookingRepository).findById(2L);
        verify(userRepository).findByUsername("admin");
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void approveBooking_ThrowsException_WhenOverlapExists() {
        // Arrange
        Booking conflictingBooking = Booking.builder()
                .id(3L)
                .room(testRoom)
                .status(Booking.BookingStatus.APPROVED)
                .startTime(pendingBooking.getStartTime())
                .endTime(pendingBooking.getEndTime())
                .build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(pendingBooking));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(bookingRepository.existsActiveOverlap(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(true);
        when(bookingRepository.findActiveBookingsInRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(conflictingBooking));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> bookingService.approveBooking(1L, "admin", "Test reason"));

        assertEquals("Cannot approve: booking now overlaps with another approved booking", exception.getMessage());
        verify(bookingRepository).findById(1L);
        verify(userRepository).findByUsername("admin");
        verify(bookingRepository, never()).save(any());
    }

    // ================== REJECTION TESTS ==================

    @Test
    void rejectBooking_Success_WithAdminUser() {
        // Arrange
        String reason = "Room not suitable for this event";

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(pendingBooking));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(bookingRepository.save(any(Booking.class))).thenReturn(pendingBooking);

        // Act
        BookingResponseDTO result = bookingService.rejectBooking(1L, "admin", reason);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("REJECTED", result.getStatus());
        assertEquals(Booking.BookingStatus.REJECTED, pendingBooking.getStatus());

        verify(bookingRepository).findById(1L);
        verify(userRepository).findByUsername("admin");
        verify(bookingRepository).save(pendingBooking);
        verify(bookingHistoryRepository).save(any(BookingHistory.class));
    }

    @Test
    void rejectBooking_Success_WithNullReason() {
        // Arrange
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(pendingBooking));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(bookingRepository.save(any(Booking.class))).thenReturn(pendingBooking);

        // Act
        BookingResponseDTO result = bookingService.rejectBooking(1L, "admin", null);

        // Assert
        assertNotNull(result);
        assertEquals("REJECTED", result.getStatus());
        assertEquals(Booking.BookingStatus.REJECTED, pendingBooking.getStatus());

        verify(bookingRepository).save(pendingBooking);
    }

    @Test
    void rejectBooking_ThrowsException_WhenBookingNotFound() {
        // Arrange
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> bookingService.rejectBooking(999L, "admin", "Test reason"));

        assertEquals("Booking not found", exception.getMessage());
        verify(bookingRepository).findById(999L);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void rejectBooking_ThrowsException_WhenUserNotAdmin() {
        // Arrange
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(pendingBooking));
        when(userRepository.findByUsername("student")).thenReturn(Optional.of(regularUser));

        // Act & Assert
        UnauthorizedActionException exception = assertThrows(UnauthorizedActionException.class,
                () -> bookingService.rejectBooking(1L, "student", "Test reason"));

        assertEquals("Only admins can reject bookings", exception.getMessage());
        verify(bookingRepository).findById(1L);
        verify(userRepository).findByUsername("student");
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void rejectBooking_ThrowsException_WhenBookingNotPending() {
        // Arrange
        when(bookingRepository.findById(2L)).thenReturn(Optional.of(approvedBooking));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> bookingService.rejectBooking(2L, "admin", "Test reason"));

        assertEquals("Can only reject PENDING bookings. Current status: APPROVED", exception.getMessage());
        verify(bookingRepository).findById(2L);
        verify(userRepository).findByUsername("admin");
        verify(bookingRepository, never()).save(any());
    }

    // ================== BOOKING HISTORY TESTS ==================

    @Test
    void getBookingHistory_Success_ReturnsAllHistory() {
        // Arrange
        List<BookingHistory> mockHistory = Arrays.asList(historyRecord);

        when(bookingRepository.existsById(1L)).thenReturn(true);
        when(bookingHistoryRepository.findByBookingIdOrderByActionAtDesc(1L)).thenReturn(mockHistory);

        // Act
        List<BookingHistoryResponseDTO> result = bookingService.getBookingHistory(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        BookingHistoryResponseDTO historyDto = result.get(0);
        assertEquals(1L, historyDto.getId());
        assertEquals(1L, historyDto.getBookingId());
        assertEquals("CREATED", historyDto.getAction());
        assertEquals(2L, historyDto.getActionByUserId());
        assertEquals("John Doe", historyDto.getActionByUserName());

        verify(bookingRepository).existsById(1L);
        verify(bookingHistoryRepository).findByBookingIdOrderByActionAtDesc(1L);
    }

    @Test
    void getBookingHistory_Success_ReturnsEmptyList() {
        // Arrange
        when(bookingRepository.existsById(1L)).thenReturn(true);
        when(bookingHistoryRepository.findByBookingIdOrderByActionAtDesc(1L)).thenReturn(Arrays.asList());

        // Act
        List<BookingHistoryResponseDTO> result = bookingService.getBookingHistory(1L);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        assertTrue(result.isEmpty());

        verify(bookingRepository).existsById(1L);
        verify(bookingHistoryRepository).findByBookingIdOrderByActionAtDesc(1L);
    }

    @Test
    void getBookingHistory_ThrowsException_WhenBookingNotFound() {
        // Arrange
        when(bookingRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> bookingService.getBookingHistory(999L));

        assertEquals("Booking not found", exception.getMessage());
        verify(bookingRepository).existsById(999L);
        verify(bookingHistoryRepository, never()).findByBookingIdOrderByActionAtDesc(any());
    }

    // ================== EDGE CASE TESTS ==================

    @Test
    void approveBooking_HandlesEmptyReason() {
        // Arrange
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(pendingBooking));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(bookingRepository.existsActiveOverlap(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(bookingRepository.findActiveBookingsInRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList());
        when(bookingRepository.save(any(Booking.class))).thenReturn(pendingBooking);

        // Act
        BookingResponseDTO result = bookingService.approveBooking(1L, "admin", "");

        // Assert
        assertNotNull(result);
        assertEquals("APPROVED", result.getStatus());
        assertEquals(Booking.BookingStatus.APPROVED, pendingBooking.getStatus());

        verify(bookingRepository).save(pendingBooking);
        verify(bookingHistoryRepository).save(any(BookingHistory.class));
    }

    @Test
    void approveBooking_DoesNotConflictWithSelf() {
        // Arrange - the booking being approved should not conflict with itself
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(pendingBooking));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(bookingRepository.existsActiveOverlap(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(true); // There IS an overlap detected
        when(bookingRepository.findActiveBookingsInRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(pendingBooking)); // But it's only with itself
        when(bookingRepository.save(any(Booking.class))).thenReturn(pendingBooking);

        // Act - should succeed because the only "conflict" is with itself
        BookingResponseDTO result = bookingService.approveBooking(1L, "admin", "Self conflict test");

        // Assert
        assertNotNull(result);
        assertEquals("APPROVED", result.getStatus());

        verify(bookingRepository).save(pendingBooking);
    }

    @Test
    void bookingHistoryLogging_CreatesCorrectHistoryRecord() {
        // Arrange
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(pendingBooking));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(bookingRepository.existsActiveOverlap(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(bookingRepository.findActiveBookingsInRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList());
        when(bookingRepository.save(any(Booking.class))).thenReturn(pendingBooking);

        // Capture the history record that gets saved
        when(bookingHistoryRepository.save(any(BookingHistory.class))).thenAnswer(invocation -> {
            BookingHistory history = invocation.getArgument(0);
            // Verify the history record has correct data
            assertEquals(pendingBooking, history.getBooking());
            assertEquals(adminUser, history.getActionBy());
            assertEquals(BookingHistory.BookingHistoryAction.APPROVED, history.getAction());
            assertEquals("Test approval reason", history.getReason());
            assertEquals(Booking.BookingStatus.APPROVED, history.getNewStatus());
            return history;
        });

        // Act
        bookingService.approveBooking(1L, "admin", "Test approval reason");

        // Assert
        verify(bookingHistoryRepository).save(any(BookingHistory.class));
    }

    @Test
    void multipleRoleUser_AdminRoleDetectedCorrectly() {
        // Arrange - user with both STUDENT and ADMIN roles
        Set<Role> multipleRoles = new HashSet<>();
        multipleRoles.add(userRole);  // STUDENT
        multipleRoles.add(adminRole); // ADMIN

        User multiRoleUser = User.builder()
                .id(3L)
                .username("multi")
                .roles(multipleRoles)
                .firstName("Multi")
                .lastName("Role")
                .build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(pendingBooking));
        when(userRepository.findByUsername("multi")).thenReturn(Optional.of(multiRoleUser));
        when(bookingRepository.existsActiveOverlap(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(bookingRepository.findActiveBookingsInRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList());
        when(bookingRepository.save(any(Booking.class))).thenReturn(pendingBooking);

        // Act - should succeed because user has ADMIN role
        BookingResponseDTO result = bookingService.approveBooking(1L, "multi", "Multi-role test");

        // Assert
        assertNotNull(result);
        assertEquals("APPROVED", result.getStatus());

        verify(bookingRepository).save(pendingBooking);
    }
}