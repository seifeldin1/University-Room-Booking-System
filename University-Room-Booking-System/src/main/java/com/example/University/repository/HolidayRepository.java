package com.example.University.repository;

import com.example.University.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {


    Optional<Holiday> findByDateAndIsActiveTrue(LocalDate date);

    boolean existsByDate(@Param("date") LocalDate date);

    boolean existsByDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    List<Holiday> findHolidaysByDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    List<Holiday> findByIsActiveTrueOrderByDateAsc();


}