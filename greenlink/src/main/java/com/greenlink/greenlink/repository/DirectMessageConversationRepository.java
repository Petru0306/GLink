package com.greenlink.greenlink.repository;

import com.greenlink.greenlink.model.DirectMessageConversation;
import com.greenlink.greenlink.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DirectMessageConversationRepository extends JpaRepository<DirectMessageConversation, Long> {
    
    /**
     * Find conversation between two users
     */
    @Query("SELECT dmc FROM DirectMessageConversation dmc WHERE " +
           "(dmc.user1 = :user1 AND dmc.user2 = :user2) OR " +
           "(dmc.user1 = :user2 AND dmc.user2 = :user1)")
    Optional<DirectMessageConversation> findConversationBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);
    
    /**
     * Find all conversations for a user
     */
    @Query("SELECT dmc FROM DirectMessageConversation dmc WHERE dmc.user1 = :user OR dmc.user2 = :user ORDER BY dmc.updatedAt DESC")
    List<DirectMessageConversation> findConversationsByUser(@Param("user") User user);
    
    /**
     * Check if conversation exists between two users
     */
    @Query("SELECT COUNT(dmc) > 0 FROM DirectMessageConversation dmc WHERE " +
           "(dmc.user1 = :user1 AND dmc.user2 = :user2) OR " +
           "(dmc.user1 = :user2 AND dmc.user2 = :user1)")
    boolean existsConversationBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);
} 