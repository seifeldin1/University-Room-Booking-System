package com.example.University.service.impl;

import com.example.University.dto.RoomFeatureDto;
import com.example.University.entity.RoomFeature;
import com.example.University.repository.FeatureRepository;
import com.example.University.service.FeatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeatureServiceImpl implements FeatureService {

    private final FeatureRepository featureRepository;

    @Override
    public RoomFeature create(RoomFeatureDto dto) {
        if (featureRepository.existsByName(dto.getName())) {
            throw new RuntimeException("Feature with name '" + dto.getName() + "' already exists");
        }
        RoomFeature feature = RoomFeature.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .build();
        return featureRepository.save(feature);
    }

    @Override
    public List<RoomFeature> getAll() {
        return featureRepository.findAll();
    }

    @Override
    public RoomFeature getById(Long id) {
        return featureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feature not found with id " + id));
    }

    @Override
    public RoomFeature update(Long id, RoomFeatureDto dto) {
        RoomFeature feature = featureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feature not found with id " + id));

        feature.setName(dto.getName());
        feature.setDescription(dto.getDescription());
        return featureRepository.save(feature);
    }

    @Override
    public void delete(Long id) {
        featureRepository.deleteById(id);
    }
}
