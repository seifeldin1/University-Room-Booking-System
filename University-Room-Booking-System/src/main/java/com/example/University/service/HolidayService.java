package com.example.University.service;

import com.example.University.dto.CreateHolidayRequestDto;
import com.example.University.dto.HolidayListResponseDto;
import com.example.University.dto.HolidayResponseDto;
import com.example.University.dto.UpdateHolidayRequestDto;

import java.time.LocalDate;
import java.util.List;

public interface HolidayService {
    HolidayResponseDto createHoliday(CreateHolidayRequestDto request, String createdBy);
    HolidayResponseDto updateHoliday(Long id, UpdateHolidayRequestDto request);
    void deleteHoliday(Long id);
    HolidayResponseDto getHolidayById(Long id);
    List<HolidayListResponseDto> getAllActiveHolidays();
    List<HolidayListResponseDto> getHolidaysByDateBetween(LocalDate startDate, LocalDate endDate);
    boolean existsByDate(LocalDate date);
    boolean existsByDateBetween(LocalDate startDate, LocalDate endDate);
}