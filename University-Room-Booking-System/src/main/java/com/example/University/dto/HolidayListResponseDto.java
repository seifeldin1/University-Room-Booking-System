package com.example.University.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HolidayListResponseDto {
    private Long id;
    private String name;
    private LocalDate date;
    private String description;
    private Boolean isRecurring;
    private Boolean isActive;
}
