package com.example.University.service.impl;

import com.example.University.dto.BuildingDto;
import com.example.University.entity.Building;
import com.example.University.repository.BuildingRepository;
import com.example.University.service.BuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BuildingServiceImpl implements BuildingService {
    private final BuildingRepository buildingRepository;

    @Autowired
    public BuildingServiceImpl(BuildingRepository buildingRepository){
        this.buildingRepository = buildingRepository;
    }

    @Override
    public Building create(BuildingDto dto) {
        if (buildingRepository.existsByCode(dto.getCode())) {
            throw new RuntimeException("Building code already exists");
        }
        Building building = Building.builder()
                .name(dto.getName())
                .code(dto.getCode())
                .address(dto.getAddress())
                .totalFloors(dto.getTotalFloors())
                .build();
        return buildingRepository.save(building);
    }

    @Override
    public List<Building> getAll() {
        return buildingRepository.findAll();
    }

    @Override
    public Building getBuildingById(Long Id){
        return buildingRepository.findById(Id)
                .orElseThrow(()-> new RuntimeException("Building not found"));
    }

    @Override
    public Building update(Long id, BuildingDto dto) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Building not found"));
        building.setName(dto.getName());
        building.setCode(dto.getCode());
        building.setAddress(dto.getAddress());
        building.setTotalFloors(dto.getTotalFloors());
        return buildingRepository.save(building);
    }

    @Override
    public void delete(Long id) {
        buildingRepository.deleteById(id);
    }
}
