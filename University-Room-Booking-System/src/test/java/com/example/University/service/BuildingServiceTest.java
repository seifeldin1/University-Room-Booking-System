package com.example.University.service;

import com.example.University.dto.BuildingDto;
import com.example.University.entity.Building;
import com.example.University.exception.ResourceNotFoundException;
import com.example.University.repository.BuildingRepository;
import com.example.University.service.impl.BuildingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuildingServiceTest {

    @Mock
    private BuildingRepository buildingRepository;

    @InjectMocks
    private BuildingServiceImpl buildingService;

    private Building testBuilding;
    private BuildingDto testBuildingDto;

    @BeforeEach
    void setUp() {
        testBuilding = Building.builder()
                .id(1L)
                .name("Library")
                .code("B01")
                .address("Main Street")
                .totalFloors(3)
                .build();

        testBuildingDto = new BuildingDto();
        testBuildingDto.setName("Library");
        testBuildingDto.setCode("B01");
        testBuildingDto.setAddress("Main Street");
        testBuildingDto.setTotalFloors(3);
        testBuildingDto.setDepartmentId(null); // optional
    }

    @Test
    void testCreateBuilding_Success() {
        when(buildingRepository.existsByCode("B01")).thenReturn(false);
        when(buildingRepository.save(any(Building.class))).thenReturn(testBuilding);

        Building result = buildingService.create(testBuildingDto);

        assertNotNull(result);
        assertEquals("Library", result.getName());
        assertEquals("B01", result.getCode());
        verify(buildingRepository).save(any(Building.class));
    }

    @Test
    void testCreateBuilding_Fail_DuplicateCode() {
        when(buildingRepository.existsByCode("B01")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> buildingService.create(testBuildingDto));

        assertEquals("Building code already exists", exception.getMessage());
        verify(buildingRepository, never()).save(any(Building.class));
    }

    @Test
    void testGetBuildingById_Found() {
        when(buildingRepository.findById(1L)).thenReturn(Optional.of(testBuilding));

        Building result = buildingService.getBuildingById(1L);

        assertNotNull(result);
        assertEquals("Library", result.getName());
        assertEquals("B01", result.getCode());
    }

    @Test
    void testGetBuildingById_NotFound() {
        when(buildingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> buildingService.getBuildingById(1L));
    }
}
