package com.example.University.service;

import com.example.University.dto.RoomDto;
import com.example.University.entity.Building;
import com.example.University.entity.Room;
import com.example.University.entity.RoomFeature;
import com.example.University.repository.BuildingRepository;
import com.example.University.repository.FeatureRepository;
import com.example.University.repository.RoomRepository;
import com.example.University.service.impl.RoomServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private BuildingRepository buildingRepository;

    @Mock
    private FeatureRepository featureRepository;

    @InjectMocks
    private RoomServiceImpl roomService;

    private Building testBuilding;
    private Room testRoom;
    private RoomDto testRoomDto;
    private RoomFeature testFeature;

    @BeforeEach
    void setUp() {
        testBuilding = Building.builder()
                .id(1L)
                .name("Main Building")
                .code("MB01")
                .address("Campus Road")
                .totalFloors(5)
                .build();

        testFeature = RoomFeature.builder()
                .id(1L)
                .name("Projector")
                .description("HD Projector")
                .build();

        testRoom = Room.builder()
                .id(1L)
                .name("Room A")
                .roomNumber("101")
                .capacity(50)
                .floorNumber(1)
                .roomType(Room.RoomType.CLASSROOM)
                .isActive(true)
                .description("Lecture room")
                .building(testBuilding)
                .features(new HashSet<>(Set.of(testFeature)))
                .build();

        testRoomDto = new RoomDto();
        testRoomDto.setName("Room A");
        testRoomDto.setRoomNumber("101");
        testRoomDto.setCapacity(50);
        testRoomDto.setFloorNumber(1);
        testRoomDto.setRoomType(Room.RoomType.CLASSROOM);
        testRoomDto.setIsActive(true);
        testRoomDto.setDescription("Lecture room");
        testRoomDto.setBuildingId(1L);
        testRoomDto.setFeatureIds(Set.of(1L));
    }

    @Test
    void testCreateRoom_Success() {
        when(buildingRepository.findById(1L)).thenReturn(Optional.of(testBuilding));
        when(featureRepository.findAllById(Set.of(1L))).thenReturn(List.of(testFeature));
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

        Room result = roomService.create(testRoomDto);

        assertNotNull(result);
        assertEquals("Room A", result.getName());
        assertEquals("101", result.getRoomNumber());
        assertEquals(testBuilding, result.getBuilding());
        verify(roomRepository).save(any(Room.class));
    }

    @Test
    void testCreateRoom_BuildingNotFound() {
        when(buildingRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> roomService.create(testRoomDto));

        assertEquals("Building not found", ex.getMessage());
    }

    @Test
    void testGetRoomById_Found() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));

        Room result = roomService.getRoomById(1L);

        assertNotNull(result);
        assertEquals("Room A", result.getName());
    }

    @Test
    void testGetRoomById_NotFound() {
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> roomService.getRoomById(1L));

        assertEquals("No room found", ex.getMessage());
    }

    @Test
    void testUpdateRoom_Success() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(buildingRepository.findById(1L)).thenReturn(Optional.of(testBuilding));
        when(featureRepository.findAllById(Set.of(1L))).thenReturn(List.of(testFeature));
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

        Room result = roomService.update(1L, testRoomDto);

        assertNotNull(result);
        assertEquals("Room A", result.getName());
        verify(roomRepository).save(testRoom);
    }

    @Test
    void testDeleteRoom_Success() {
        doNothing().when(roomRepository).deleteById(1L);

        roomService.delete(1L);

        verify(roomRepository).deleteById(1L);
    }

    @Test
    void testAttachFeatures_Success() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(featureRepository.findAllById(List.of(1L))).thenReturn(List.of(testFeature));
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

        Room result = roomService.attachFeatures(1L, List.of(1L));

        assertTrue(result.getFeatures().contains(testFeature));
        verify(roomRepository).save(testRoom);
    }

    @Test
    void testDetachFeature_Success() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(featureRepository.findById(1L)).thenReturn(Optional.of(testFeature));
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

        Room result = roomService.detachFeature(1L, 1L);

        assertFalse(result.getFeatures().contains(testFeature));
        verify(roomRepository).save(testRoom);
    }
}
