package com.example.University.service;

import com.example.University.dto.CreateHolidayRequestDto;
import com.example.University.dto.HolidayResponseDto;
import com.example.University.entity.Holiday;
import com.example.University.entity.User;
import com.example.University.exception.ResourceNotFoundException;
import com.example.University.repository.HolidayRepository;
import com.example.University.repository.UserRepository;
import com.example.University.service.impl.HolidayServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HolidayServiceTest {

    @Mock
    private HolidayRepository holidayRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private HolidayServiceImpl holidayService;

    private User testUser;
    private Holiday testHoliday;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("admin@university.edu");
        testHoliday = new Holiday();
        testHoliday.setId(1L);
        testHoliday.setName("Test Holiday");
        testHoliday.setDate(LocalDate.of(2025, 12, 25));
        testHoliday.setIsActive(true);
        testHoliday.setCreatedBy(testUser);
        testHoliday.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateHoliday_Success() {
        CreateHolidayRequestDto request = new CreateHolidayRequestDto(
                "Christmas",
                LocalDate.of(2025, 12, 25),
                "Christmas Day",
                true
        );
        when(userRepository.findByEmail("admin@university.edu")).thenReturn(Optional.of(testUser));
        when(holidayRepository.findByDateAndIsActiveTrue(any(LocalDate.class))).thenReturn(Optional.empty());
        when(holidayRepository.save(any(Holiday.class))).thenReturn(testHoliday);

        HolidayResponseDto result = holidayService.createHoliday(request, "admin@university.edu");

        assertNotNull(result);
        assertEquals("Test Holiday", result.getName());
        assertEquals(LocalDate.of(2025, 12, 25), result.getDate());
        verify(holidayRepository).save(any(Holiday.class));
    }

    @Test
    void testCreateHoliday_DuplicateDate_ThrowsException() {
        CreateHolidayRequestDto request = new CreateHolidayRequestDto(
                "Christmas",
                LocalDate.of(2025, 12, 25),
                "Christmas Day",
                false
        );

        when(holidayRepository.findByDateAndIsActiveTrue(LocalDate.of(2025, 12, 25)))
                .thenReturn(Optional.of(testHoliday));

        assertThrows(IllegalStateException.class, () ->
                holidayService.createHoliday(request, "admin@university.edu"));
    }

    @Test
    void testCreateHoliday_UserNotFound_ThrowsException() {
        CreateHolidayRequestDto request = new CreateHolidayRequestDto(
                "Christmas",
                LocalDate.of(2025, 12, 25),
                "Christmas Day",
                false
        );

        when(userRepository.findByEmail("admin@university.edu")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                holidayService.createHoliday(request, "admin@university.edu"));
    }

    @Test
    void testIsHoliday_ReturnsTrue() {
        // Arrange
        when(holidayRepository.existsByDate(LocalDate.of(2025, 12, 25))).thenReturn(true);

        // Act
        boolean result = holidayService.existsByDate(LocalDate.of(2025, 12, 25));

        // Assert
        assertTrue(result);
    }

    @Test
    void testIsHoliday_ReturnsFalse() {
        when(holidayRepository.existsByDate(LocalDate.of(2025, 12, 26))).thenReturn(false);

        boolean result = holidayService.existsByDate(LocalDate.of(2025, 12, 26));

        assertFalse(result);
    }
}