package com.example.University.service;

import com.example.University.dto.RoomFeatureDto;
import com.example.University.entity.RoomFeature;

import java.util.List;

public interface FeatureService {
    RoomFeature create(RoomFeatureDto dto);
    List<RoomFeature> getAll();
    RoomFeature getById(Long id);
    RoomFeature update(Long id, RoomFeatureDto dto);
    void delete(Long id);
}
