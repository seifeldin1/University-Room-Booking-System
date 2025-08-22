package com.example.University.controller;

import com.example.University.dto.BuildingDto;
import com.example.University.entity.Building;
import com.example.University.service.BuildingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/buildings")
@RequiredArgsConstructor
public class BuildingController {
    private final BuildingService buildingService;

    @PostMapping
    public ResponseEntity<Building> create(@RequestBody BuildingDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(buildingService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<Building>> getAll() {
        return ResponseEntity.ok(buildingService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Building> getById(@PathVariable Long id){
        Building buildObject = buildingService.getBuildingById(id);
        return ResponseEntity.ok(buildObject);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Building> update(@PathVariable Long id, @RequestBody BuildingDto dto) {
        return ResponseEntity.ok(buildingService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        buildingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}