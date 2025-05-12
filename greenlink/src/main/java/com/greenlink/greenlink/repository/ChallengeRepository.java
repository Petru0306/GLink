package com.greenlink.greenlink.repository;

import com.greenlink.greenlink.model.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    List<Challenge> findByUserIdAndStatus(Long userId, Challenge.ChallengeStatus status);
    Optional<Challenge> findByIdAndUserId(Long id, Long userId);
}