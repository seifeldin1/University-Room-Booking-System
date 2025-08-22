package com.example.University.repository;

import com.example.University.entity.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BuildingRepository extends JpaRepository<Building , Long> {
    boolean existsByCode(String code);
    boolean existsByName(String name);
}
