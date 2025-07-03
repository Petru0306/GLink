package com.greenlink.greenlink.repository;

import com.greenlink.greenlink.model.RecyclingPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RecyclingPointRepository extends JpaRepository<RecyclingPoint, Long> {
    
    List<RecyclingPoint> findByNameContainingIgnoreCaseOrAddressContainingIgnoreCase(String name, String address);
    
    @Query("SELECT rp FROM RecyclingPoint rp WHERE :material MEMBER OF rp.materialsAccepted")
    List<RecyclingPoint> findByMaterialsAcceptedContaining(String material);
}
