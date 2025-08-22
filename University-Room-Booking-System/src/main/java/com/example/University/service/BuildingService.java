package com.example.University.service;

import com.example.University.dto.BuildingDto;
import com.example.University.entity.Building;

import java.util.List;

public interface BuildingService {
    Building create(BuildingDto building);
    List<Building> getAll();
    Building update(Long id , BuildingDto building);
    void delete(Long id);
    Building getBuildingById(Long id);
}
