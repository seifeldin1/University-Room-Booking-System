package com.example.University.repository;

import com.example.University.entity.Booking;
import com.example.University.entity.BookingHistory;
import com.example.University.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingHistoryRepository extends JpaRepository<BookingHistory, Long> {

    List<BookingHistory> findByBookingOrderByActionAtDesc(Booking booking);

    List<BookingHistory> findByBookingIdOrderByActionAtDesc(Long bookingId);

    List<BookingHistory> findByActionOrderByActionAtDesc(BookingHistory.BookingHistoryAction action);

    List<BookingHistory> findByActionByOrderByActionAtDesc(User actionBy);

    @Query("SELECT bh FROM BookingHistory bh ORDER BY bh.actionAt DESC")
    List<BookingHistory> findRecentHistory(Pageable pageable);

    long countByBookingId(Long bookingId);

    @Query("SELECT bh FROM BookingHistory bh WHERE bh.action = 'CANCELLED' ORDER BY bh.actionAt DESC")
    List<BookingHistory> findAllCancellations();

    @Query("SELECT bh FROM BookingHistory bh WHERE " +
            "(:bookingId IS NULL OR bh.booking.id = :bookingId) AND " +
            "(:actionBy IS NULL OR bh.actionBy.id = :actionBy) AND " +
            "(:action IS NULL OR bh.action = :action) AND " +
            "(:startDate IS NULL OR bh.actionAt >= :startDate) AND " +
            "(:endDate IS NULL OR bh.actionAt <= :endDate) " +
            "ORDER BY bh.actionAt DESC")
    Page<BookingHistory> findHistoryByCriteria(
            @Param("bookingId") Long bookingId,
            @Param("actionBy") Long actionBy,
            @Param("action") BookingHistory.BookingHistoryAction action,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    void deleteByActionAtBefore(LocalDateTime cutoffDate);

    @Query("SELECT bh FROM BookingHistory bh WHERE bh.booking.id = :bookingId ORDER BY bh.actionAt DESC LIMIT 1")
    BookingHistory findLatestActionForBooking(@Param("bookingId") Long bookingId);
}