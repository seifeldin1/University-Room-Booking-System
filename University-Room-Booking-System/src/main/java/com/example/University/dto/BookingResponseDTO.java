package com.example.University.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookingResponseDTO {
    private Long id;
    private Long roomId;
    private Long userId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String purpose;
    private String status; // PENDING, APPROVED, etc.
}

