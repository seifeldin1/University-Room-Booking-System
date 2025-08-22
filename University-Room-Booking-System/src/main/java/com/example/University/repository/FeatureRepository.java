package com.example.University.repository;

import com.example.University.entity.RoomFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeatureRepository extends JpaRepository<RoomFeature , Long> {
    boolean existsByName(String name);
}
