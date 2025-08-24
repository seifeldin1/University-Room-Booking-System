package com.example.University.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingHistoryResponseDTO {
    private Long id;
    private Long bookingId;
    private String action;
    private Long actionByUserId;
    private String actionByUserName;
    private LocalDateTime actionAt;
    private String reason;
    private String previousStatus;
    private String newStatus;
}