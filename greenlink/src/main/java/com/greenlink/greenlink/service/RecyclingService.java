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

    // Preia toate punctele de reciclare
    public List<RecyclingPointDto> getAllRecyclingPoints() {
        return recyclingPointRepository.findAll().stream()
                .map(point -> new RecyclingPointDto(
                        point.getId(),
                        point.getName(),
                        point.getAddress(),
                        point.getMaterialsAccepted()
                ))
                .collect(Collectors.toList());
    }

    // Preia un punct de reciclare după ID
    public RecyclingPointDto getRecyclingPointById(Long id) {
        RecyclingPoint point = recyclingPointRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Punctul de reciclare nu a fost găsit"));
        return new RecyclingPointDto(
                point.getId(),
                point.getName(),
                point.getAddress(),
                point.getMaterialsAccepted()
        );
    }

    // Adaugă un punct de reciclare nou
    public RecyclingPointDto addRecyclingPoint(RecyclingPointDto recyclingPointDto) {
        RecyclingPoint point = new RecyclingPoint(
                null,
                recyclingPointDto.getName(),
                recyclingPointDto.getAddress(),
                recyclingPointDto.getMaterialsAccepted()
        );
        RecyclingPoint savedPoint = recyclingPointRepository.save(point);
        return new RecyclingPointDto(
                savedPoint.getId(),
                savedPoint.getName(),
                savedPoint.getAddress(),
                savedPoint.getMaterialsAccepted()
        );
    }
}
