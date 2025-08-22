package com.example.University.dto;

import com.example.University.entity.Room.RoomType;
import lombok.Data;

import java.util.Set;

@Data
public class RoomDto {
    private String name;
    private String roomNumber;
    private Integer capacity;
    private Integer floorNumber;
    private RoomType roomType;
    private Boolean isActive = true;
    private String description;
    private Long buildingId;
    private Set<Long> featureIds;
}
