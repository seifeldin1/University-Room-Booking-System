package com.example.University.controller;

import com.example.University.dto.RoomFeatureDto;
import com.example.University.entity.RoomFeature;
import com.example.University.service.FeatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/features")
@RequiredArgsConstructor
public class FeatureController {

    private final FeatureService featureService;

    @PostMapping
    public ResponseEntity<RoomFeature> create(@RequestBody RoomFeatureDto dto) {
        return ResponseEntity.ok(featureService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<RoomFeature>> getAll() {
        return ResponseEntity.ok(featureService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomFeature> getById(@PathVariable Long id) {
        return ResponseEntity.ok(featureService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoomFeature> update(@PathVariable Long id, @RequestBody RoomFeatureDto dto) {
        return ResponseEntity.ok(featureService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        featureService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
