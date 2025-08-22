package com.example.University.service.impl;

import com.example.University.dto.RoomDto;
import com.example.University.entity.Building;
import com.example.University.entity.Room;
import com.example.University.entity.RoomFeature;
import com.example.University.repository.BuildingRepository;
import com.example.University.repository.FeatureRepository;
import com.example.University.repository.RoomRepository;
import com.example.University.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
    private final RoomRepository roomRepository;
    private final BuildingRepository buildingRepository;
    private final FeatureRepository featureRepository;

    @Override
    public Room create(RoomDto dto) {
        Building building = buildingRepository.findById(dto.getBuildingId())
                .orElseThrow(() -> new RuntimeException("Building not found"));

        Set<RoomFeature> features = new HashSet<>();
        if (dto.getFeatureIds() != null) {
            features = new HashSet<>(featureRepository.findAllById(dto.getFeatureIds()));
        }

        Room room = Room.builder()
                .name(dto.getName())
                .roomNumber(dto.getRoomNumber())
                .capacity(dto.getCapacity())
                .floorNumber(dto.getFloorNumber())
                .roomType(dto.getRoomType())
                .isActive(dto.getIsActive())
                .description(dto.getDescription())
                .building(building)
                .features(features)
                .build();

        return roomRepository.save(room);
    }

    @Override
    public List<Room> getAll() {
        return roomRepository.findAll();
    }

    @Override
    public Room getRoomById(Long Id){
        return roomRepository.findById(Id)
                            .orElseThrow(()-> new RuntimeException("No room found"));
    }
    @Override
    public Room update(Long id, RoomDto dto) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        Building building = buildingRepository.findById(dto.getBuildingId())
                .orElseThrow(() -> new RuntimeException("Building not found"));

        Set<RoomFeature> features = new HashSet<>();
        if (dto.getFeatureIds() != null) {
            features = new HashSet<>(featureRepository.findAllById(dto.getFeatureIds()));
        }

        room.setName(dto.getName());
        room.setRoomNumber(dto.getRoomNumber());
        room.setCapacity(dto.getCapacity());
        room.setFloorNumber(dto.getFloorNumber());
        room.setRoomType(dto.getRoomType());
        room.setIsActive(dto.getIsActive());
        room.setDescription(dto.getDescription());
        room.setBuilding(building);
        room.setFeatures(features);

        return roomRepository.save(room);
    }

    @Override
    public void delete(Long id) {
        // TODO: Add check for future approved bookings once Booking is implemented
        roomRepository.deleteById(id);
    }

    @Override
    public Room attachFeatures(Long roomId, List<Long> featureIds) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        Set<RoomFeature> features = new HashSet<>(featureRepository.findAllById(featureIds));
        room.getFeatures().addAll(features);
        return roomRepository.save(room);
    }

    @Override
    public Room detachFeature(Long roomId, Long featureId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        RoomFeature feature = featureRepository.findById(featureId)
                .orElseThrow(() -> new RuntimeException("Feature not found"));

        room.getFeatures().remove(feature);
        return roomRepository.save(room);
    }
}
