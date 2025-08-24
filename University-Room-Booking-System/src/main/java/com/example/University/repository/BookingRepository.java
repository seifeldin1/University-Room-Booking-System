package com.example.University.repository;

import com.example.University.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
           SELECT (COUNT(b) > 0)
           FROM Booking b
           WHERE b.room.id = :roomId
             AND b.status IN ('APPROVED', 'PENDING')
             AND b.startTime < :end
             AND b.endTime > :start
           """)
    boolean existsActiveOverlap(
            @Param("roomId") Long roomId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * Returns the conflicting bookings (useful for error messages or availability display).
     */
    @Query("""
           SELECT b
           FROM Booking b
           WHERE b.room.id = :roomId
             AND b.status IN ('APPROVED', 'PENDING')
             AND b.startTime < :end
             AND b.endTime > :start
           ORDER BY b.startTime ASC
           """)
    List<Booking> findActiveOverlaps(
            @Param("roomId") Long roomId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
    // Get all bookings for a room within a date range

    @Query("""
            SELECT b 
            FROM Booking b 
            WHERE b.room.id = :roomId 
              AND b.status IN ('APPROVED', 'PENDING')
              AND b.startTime < :endTime 
              AND b.endTime > :startTime 
           ORDER BY b.startTime ASC """)
    List<Booking> findActiveBookingsInRange( @Param("roomId") Long roomId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime );

    @Query("""
            SELECT b FROM Booking b 
            WHERE b.user.id = :userId 
            AND b.status IN ('PENDING', 'APPROVED') 
            AND b.startTime > :currentTime
            ORDER BY b.startTime ASC
            """)
    List<Booking> findCancellableBookingsByUser(
            @Param("userId") Long userId,
            @Param("currentTime") LocalDateTime currentTime);

}
