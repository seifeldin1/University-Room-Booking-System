package com.example.University.service;

import com.example.University.dto.RoomDto;
import com.example.University.entity.Room;

import java.util.List;

public interface RoomService {
    Room create(RoomDto room);
    List<Room> getAll();
    Room getRoomById(Long Id);
    Room update(Long Id , RoomDto room);
    void delete(Long Id);
    Room attachFeatures(Long Id , List<Long> featureIds);
    Room detachFeature(Long Id , Long featureId);
}
