package com.example.University.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// Request DTOs
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateHolidayRequestDto {

    @NotBlank(message = "Holiday name is required")
    private String name;

    @NotNull(message = "Holiday date is required")
    private LocalDate date;

    private String description;

    private Boolean isRecurring = false;
}

