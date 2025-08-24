package com.example.University.service;

import com.example.University.dto.RoomFeatureDto;
import com.example.University.entity.RoomFeature;
import com.example.University.entity.User;
import com.example.University.exception.ResourceNotFoundException;
import com.example.University.repository.FeatureRepository;
import com.example.University.repository.UserRepository;
import com.example.University.service.impl.FeatureServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeatureServiceTest {

    @Mock
    private FeatureRepository featureRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FeatureServiceImpl featureService;

    private RoomFeature feature;

    @BeforeEach
    void setUp() {
        feature = RoomFeature.builder()
                .id(1L)
                .name("Projector")
                .description("HD Projector")
                .build();
    }

    @Test
    void testCreateFeature() {
        RoomFeatureDto dto = new RoomFeatureDto();
        dto.setName("Whiteboard");
        dto.setDescription("Magnetic Whiteboard");

        when(featureRepository.save(any(RoomFeature.class))).thenAnswer(invocation -> {
            RoomFeature f = invocation.getArgument(0);
            f.setId(2L);
            return f;
        });

        RoomFeature created = featureService.create(dto);

        assertNotNull(created);
        assertEquals("Whiteboard", created.getName());
        assertEquals("Magnetic Whiteboard", created.getDescription());
        assertEquals(2L, created.getId());
        verify(featureRepository, times(1)).save(any(RoomFeature.class));
    }

    @Test
    void testGetFeatureById_Success() {
        when(featureRepository.findById(1L)).thenReturn(Optional.of(feature));

        RoomFeature result = featureService.getById(1L);

        assertNotNull(result);
        assertEquals("Projector", result.getName());
        verify(featureRepository, times(1)).findById(1L);
    }

    @Test
    void testGetFeatureById_NotFound() {
        when(featureRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> featureService.getById(1L));
    }

    @Test
    void testGetAllFeatures() {
        when(featureRepository.findAll()).thenReturn(List.of(feature));

        List<RoomFeature> features = featureService.getAll();

        assertEquals(1, features.size());
        assertEquals("Projector", features.get(0).getName());
        verify(featureRepository, times(1)).findAll();
    }

    @Test
    void testUpdateFeature_Success() {
        RoomFeatureDto dto = new RoomFeatureDto();
        dto.setName("Smart Board");
        dto.setDescription("Interactive Smart Board");

        when(featureRepository.findById(1L)).thenReturn(Optional.of(feature));
        when(featureRepository.save(any(RoomFeature.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RoomFeature updated = featureService.update(1L, dto);

        assertNotNull(updated);
        assertEquals("Smart Board", updated.getName());
        assertEquals("Interactive Smart Board", updated.getDescription());
        verify(featureRepository, times(1)).save(any(RoomFeature.class));
    }

    @Test
    void testUpdateFeature_NotFound() {
        RoomFeatureDto dto = new RoomFeatureDto();
        dto.setName("Smart Board");
        dto.setDescription("Interactive Smart Board");

        when(featureRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> featureService.update(1L, dto));
    }

    @Test
    void testDeleteFeature_Success() {
        when(featureRepository.findById(1L)).thenReturn(Optional.of(feature));
        doNothing().when(featureRepository).delete(feature);

        featureService.delete(1L);

        verify(featureRepository, times(1)).delete(feature);
    }

    @Test
    void testDeleteFeature_NotFound() {
        when(featureRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> featureService.delete(1L));
    }
}
