package com.example.University.controller;


import com.example.University.dto.*;
import com.example.University.service.HolidayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/holidays")
@RequiredArgsConstructor
@Slf4j
public class HolidayController {

    private final HolidayService holidayService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HolidayResponseDto> createHoliday(
            @Valid @RequestBody CreateHolidayRequestDto request,
            Authentication authentication) {
        HolidayResponseDto createdHoliday = holidayService.createHoliday(request, authentication.getName());
        return ResponseEntity
                .created(URI.create("/api/v1/holidays/" + createdHoliday.getId()))
                .body(createdHoliday);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HolidayResponseDto> updateHoliday(
            @PathVariable Long id,
            @Valid @RequestBody UpdateHolidayRequestDto request) {
        HolidayResponseDto updatedHoliday = holidayService.updateHoliday(id, request);
        return ResponseEntity.ok(updatedHoliday);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteHoliday(@PathVariable Long id) {
        holidayService.deleteHoliday(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'FACULTY', 'ADMIN')")
    public ResponseEntity<HolidayResponseDto> getHolidayById(@PathVariable Long id) {
        HolidayResponseDto holiday = holidayService.getHolidayById(id);
        return ResponseEntity.ok(holiday);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'FACULTY', 'ADMIN')")
    public ResponseEntity<List<HolidayListResponseDto>> getAllActiveHolidays() {
        List<HolidayListResponseDto> holidays = holidayService.getAllActiveHolidays();
        return ResponseEntity.ok(holidays);
    }

    @GetMapping("/range")
    @PreAuthorize("hasAnyRole('STUDENT', 'FACULTY', 'ADMIN')")
    public ResponseEntity<List<HolidayListResponseDto>> getHolidaysByDateBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().build();
        }
        List<HolidayListResponseDto> holidays = holidayService.getHolidaysByDateBetween(startDate, endDate);
        return ResponseEntity.ok(holidays);
    }

    @GetMapping("/check")
    @PreAuthorize("hasAnyRole('STUDENT', 'FACULTY', 'ADMIN')")
    public ResponseEntity<Boolean> existsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        boolean existsByDate = holidayService.existsByDate(date);
        return ResponseEntity.ok(existsByDate);
    }

    @GetMapping("/check/range")
    @PreAuthorize("hasAnyRole('STUDENT', 'FACULTY', 'ADMIN')")
    public ResponseEntity<Boolean> existsByDateBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate){

        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().build();
        }
        boolean existsByDateBetween = holidayService.existsByDateBetween(startDate,endDate);
        return ResponseEntity.ok(existsByDateBetween);
    }

}