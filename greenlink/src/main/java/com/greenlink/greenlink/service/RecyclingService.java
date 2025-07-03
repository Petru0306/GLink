package com.greenlink.greenlink.service;

import com.greenlink.greenlink.dto.RecyclingPointDto;
import com.greenlink.greenlink.model.RecyclingPoint;
import com.greenlink.greenlink.repository.RecyclingPointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecyclingService {

    @Autowired
    private RecyclingPointRepository recyclingPointRepository;

    public List<RecyclingPointDto> getAllRecyclingPoints() {
        return recyclingPointRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<RecyclingPointDto> searchRecyclingPoints(String searchTerm) {
        return recyclingPointRepository.findByNameContainingIgnoreCaseOrAddressContainingIgnoreCase(
                searchTerm, searchTerm)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<RecyclingPointDto> getRecyclingPointsByMaterial(String material) {
        return recyclingPointRepository.findByMaterialsAcceptedContaining(material)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public RecyclingPointDto getRecyclingPointById(Long id) {
        RecyclingPoint point = recyclingPointRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Punctul de reciclare nu a fost găsit"));
        return convertToDto(point);
    }

    private RecyclingPointDto convertToDto(RecyclingPoint point) {
        return new RecyclingPointDto(
                point.getId(),
                point.getName(),
                point.getAddress(),
                point.getLatitude(),
                point.getLongitude(),
                point.getMaterialsAccepted(),
                point.getDescription(),
                point.getSchedule(),
                point.getContactPhone()
        );
    }

    public RecyclingPointDto addRecyclingPoint(RecyclingPointDto dto) {
        RecyclingPoint point = new RecyclingPoint(
                dto.getName(),
                dto.getAddress(),
                dto.getLatitude(),
                dto.getLongitude(),
                dto.getMaterialsAccepted()
        );
        point.setDescription(dto.getDescription());
        point.setSchedule(dto.getSchedule());
        point.setContactPhone(dto.getContactPhone());
        
        RecyclingPoint savedPoint = recyclingPointRepository.save(point);
        return convertToDto(savedPoint);
    }
}
