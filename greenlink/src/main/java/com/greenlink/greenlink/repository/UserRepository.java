package com.greenlink.greenlink.repository;

import com.greenlink.greenlink.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query(value = "SELECT * FROM users WHERE active = true ORDER BY points DESC LIMIT :limit",
            nativeQuery = true)
    List<User> findTopByOrderByPointsDesc(int limit);
}