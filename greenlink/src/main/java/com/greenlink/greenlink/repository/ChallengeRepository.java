package com.greenlink.greenlink.repository;

import com.greenlink.greenlink.model.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    List<Challenge> findByCategory(Challenge.ChallengeCategory category);
    List<Challenge> findByUserId(Long userId);
    Optional<Challenge> findByIdAndUserId(Long id, Long userId);
    List<Challenge> findByProgressEvent(String progressEvent);
}