package com.greenlink.greenlink.repository;

import com.greenlink.greenlink.model.PointEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PointEventRepository extends JpaRepository<PointEvent, Long> {
    
    List<PointEvent> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    Page<PointEvent> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    List<PointEvent> findByUserIdAndEventTypeOrderByCreatedAtDesc(Long userId, String eventType);
    
    @Query("SELECT pe FROM PointEvent pe WHERE pe.user.id = ?1 ORDER BY pe.createdAt DESC LIMIT 5")
    List<PointEvent> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);
    
    @Query("SELECT SUM(pe.points) FROM PointEvent pe WHERE pe.user.id = ?1")
    Integer getTotalPointsEarned(Long userId);
    
    @Query("SELECT SUM(pe.points) FROM PointEvent pe WHERE pe.user.id = ?1 AND pe.eventType = ?2")
    Integer getTotalPointsByEventType(Long userId, String eventType);
    
    @Query("SELECT COUNT(pe) FROM PointEvent pe WHERE pe.user.id = ?1 AND pe.leveledUp = true")
    Long getLevelUpCount(Long userId);
    
    @Query("SELECT pe FROM PointEvent pe WHERE pe.user.id = ?1 AND pe.createdAt >= ?2 ORDER BY pe.createdAt DESC")
    List<PointEvent> findRecentEvents(Long userId, LocalDateTime since);
    
    @Query("SELECT pe.eventType, COUNT(pe) FROM PointEvent pe WHERE pe.user.id = ?1 GROUP BY pe.eventType")
    List<Object[]> getEventTypeStats(Long userId);
    
    @Query("SELECT pe FROM PointEvent pe WHERE pe.user.id = ?1 AND pe.leveledUp = true ORDER BY pe.createdAt DESC")
    List<PointEvent> findLevelUpEvents(Long userId);
} 