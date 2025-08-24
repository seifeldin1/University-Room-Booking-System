package com.example.University.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "booking_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private BookingHistoryAction action;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_by", nullable = false)
    private User actionBy;

    @Column(name = "action_at", nullable = false)
    private LocalDateTime actionAt;

    @Column(name = "reason")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status")
    private Booking.BookingStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status")
    private Booking.BookingStatus newStatus;

    @Column(name = "additional_notes")
    private String additionalNotes;

    public enum BookingHistoryAction {
        CREATED,
        APPROVED,
        REJECTED,
        CANCELLED,
        MODIFIED,
        DELETED;
    }


}
