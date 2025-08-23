package com.example.University.service.impl;

import com.example.University.dto.*;
import com.example.University.entity.Holiday;
import com.example.University.entity.User;
import com.example.University.exception.ResourceNotFoundException;
import com.example.University.repository.HolidayRepository;
import com.example.University.repository.UserRepository;
import com.example.University.service.HolidayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class HolidayServiceImpl implements HolidayService {

    private final HolidayRepository holidayRepository;
    private final UserRepository userRepository;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public HolidayResponseDto createHoliday(CreateHolidayRequestDto request, String createdBy) {

        holidayRepository.findByDateAndIsActiveTrue(request.getDate())
                .ifPresent(existing -> {
                    throw new IllegalStateException("Holiday already exists for date: " + request.getDate());
                });

        User creator = userRepository.findByEmail(createdBy)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + createdBy));

        Holiday holiday = new Holiday();
        holiday.setName(request.getName());
        holiday.setDate(request.getDate());
        holiday.setDescription(request.getDescription());
        holiday.setIsRecurring(request.getIsRecurring());
        holiday.setIsActive(true);
        holiday.setCreatedBy(creator);

        Holiday savedHoliday = holidayRepository.save(holiday);

        return mapToResponseDto(savedHoliday);
    }

    @Override
    public HolidayResponseDto updateHoliday(Long id, UpdateHolidayRequestDto request) {

        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday not found with ID: " + id));

        if (request.getName() != null) {
            holiday.setName(request.getName());
        }
        if (request.getDate() != null) {
            holidayRepository.findByDateAndIsActiveTrue(request.getDate())
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> {
                        throw new IllegalStateException("Another holiday already exists for date: " + request.getDate());
                    });
            holiday.setDate(request.getDate());
        }
        if (request.getDescription() != null) {
            holiday.setDescription(request.getDescription());
        }
        if (request.getIsRecurring() != null) {
            holiday.setIsRecurring(request.getIsRecurring());
        }
        if (request.getIsActive() != null) {
            holiday.setIsActive(request.getIsActive());
        }

        Holiday updatedHoliday = holidayRepository.save(holiday);
        return mapToResponseDto(updatedHoliday);
    }

    @Override
    public void deleteHoliday(Long id) {

        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday not found with ID: " + id));

        holiday.setIsActive(false);
        holidayRepository.save(holiday);

    }

    @Override
    @Transactional(readOnly = true)
    public HolidayResponseDto getHolidayById(Long id) {
        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday not found with ID: " + id));

        return mapToResponseDto(holiday);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HolidayListResponseDto> getAllActiveHolidays() {

        return holidayRepository.findByIsActiveTrueOrderByDateAsc()
                .stream()
                .map(this::mapToListResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HolidayListResponseDto> getHolidaysByDateBetween(LocalDate startDate, LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        return holidayRepository.findHolidaysByDateBetween(startDate, endDate)
                .stream()
                .map(this::mapToListResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByDate(LocalDate date) {
        return holidayRepository.existsByDate(date);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByDateBetween(LocalDate startDate, LocalDate endDate) {
        return holidayRepository.existsByDateBetween(startDate, endDate);
    }

    private HolidayResponseDto mapToResponseDto(Holiday holiday) {
        HolidayResponseDto dto = new HolidayResponseDto();
        dto.setId(holiday.getId());
        dto.setName(holiday.getName());
        dto.setDate(holiday.getDate());
        dto.setDescription(holiday.getDescription());
        dto.setIsRecurring(holiday.getIsRecurring());
        dto.setIsActive(holiday.getIsActive());
        dto.setCreatedBy(holiday.getCreatedBy().getEmail());
        dto.setCreatedAt(holiday.getCreatedAt().format(dateTimeFormatter));
        dto.setUpdatedAt(holiday.getUpdatedAt() != null ?
                holiday.getUpdatedAt().format(dateTimeFormatter) : null);
        return dto;
    }

    private HolidayListResponseDto mapToListResponseDto(Holiday holiday) {
        HolidayListResponseDto dto = new HolidayListResponseDto();
        dto.setId(holiday.getId());
        dto.setName(holiday.getName());
        dto.setDate(holiday.getDate());
        dto.setDescription(holiday.getDescription());
        dto.setIsRecurring(holiday.getIsRecurring());
        dto.setIsActive(holiday.getIsActive());
        return dto;
    }
}