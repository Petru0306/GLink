package com.greenlink.greenlink.repository;

import com.greenlink.greenlink.model.Challenge;
import com.greenlink.greenlink.model.UserChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserChallengeRepository extends JpaRepository<UserChallenge, Long> {
    List<UserChallenge> findByUserIdAndStatus(Long userId, Challenge.ChallengeStatus status);
    
    List<UserChallenge> findByUserId(Long userId);
    
    Optional<UserChallenge> findByUserIdAndChallengeId(Long userId, Long challengeId);
    
    @Query("SELECT COUNT(uc) FROM UserChallenge uc WHERE uc.user.id = ?1 AND uc.status = 'COMPLETED'")
    long countCompletedChallengesByUserId(Long userId);
    
    @Query(value = "SELECT COUNT(DISTINCT DATE(completed_at)) FROM user_challenges " +
           "WHERE user_id = ?1 AND status = 'COMPLETED' " +
           "AND completed_at >= CURRENT_DATE - INTERVAL '30 days'", nativeQuery = true)
    int getCurrentStreak(Long userId);
    
    @Query("SELECT SUM(c.points) FROM UserChallenge uc JOIN uc.challenge c " +
           "WHERE uc.user.id = ?1 AND uc.status = 'COMPLETED'")
    Integer getTotalPoints(Long userId);
} 