package com.greenlink.greenlink.repository;

import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.favoriteProducts WHERE u.email = :email")
    Optional<User> findByEmailWithFavorites(String email);

    @Query(value = "SELECT * FROM users WHERE active = true ORDER BY points DESC LIMIT :limit",
            nativeQuery = true)
    List<User> findTopByOrderByPointsDesc(int limit);
    
    @Query("SELECT u FROM User u JOIN u.favoriteProducts fp WHERE fp = :product")
    List<User> findByFavoriteProductsContains(@Param("product") Product product);

    List<User> findByEmailContainingIgnoreCase(String email);
    
    List<User> findByEmailContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
        String email, String firstName, String lastName);
        
    @Query("SELECT u FROM User u WHERE u.id != :currentUserId AND "
          + "(LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR "
          + "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR "
          + "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<User> searchUsers(@Param("query") String query, @Param("currentUserId") Long currentUserId);
}
