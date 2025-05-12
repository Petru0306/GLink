package com.greenlink.greenlink.repository;

import com.greenlink.greenlink.model.RecyclingPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecyclingPointRepository extends JpaRepository<RecyclingPoint, Long> {
    // Adaugă metode personalizate dacă este necesar
}
