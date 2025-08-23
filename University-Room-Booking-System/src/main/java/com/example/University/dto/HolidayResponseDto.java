package com.example.University.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// Response DTOs
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HolidayResponseDto {
    private Long id;
    private String name;
    private LocalDate date;
    private String description;
    private Boolean isRecurring;
    private Boolean isActive;
    private String createdBy;
    private String createdAt;
    private String updatedAt;
}
