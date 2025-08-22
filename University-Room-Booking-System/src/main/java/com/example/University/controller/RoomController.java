package com.example.University.controller;

import com.example.University.dto.RoomDto;
import com.example.University.entity.Room;
import com.example.University.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<Room> create(@RequestBody RoomDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<Room>> getAll() {
        return ResponseEntity.ok(roomService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> getById(@PathVariable Long id){
        Room room = roomService.getRoomById(id);
        return ResponseEntity.ok(room);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Room> update(@PathVariable Long id, @RequestBody RoomDto dto) {
        return ResponseEntity.ok(roomService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roomService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/features")
    public ResponseEntity<Room> attachFeatures(@PathVariable Long id, @RequestBody List<Long> featureIds) {
        return ResponseEntity.ok(roomService.attachFeatures(id, featureIds));
    }

    @DeleteMapping("/{id}/features/{fid}")
    public ResponseEntity<Room> detachFeature(@PathVariable Long id, @PathVariable Long fid) {
        return ResponseEntity.ok(roomService.detachFeature(id, fid));
    }
}
