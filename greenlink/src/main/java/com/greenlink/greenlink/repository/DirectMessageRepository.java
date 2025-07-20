package com.greenlink.greenlink.repository;

import com.greenlink.greenlink.model.DirectMessage;
import com.greenlink.greenlink.model.DirectMessageConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long> {
    
    /**
     * Find all messages in a conversation
     */
    List<DirectMessage> findByConversationOrderBySentAtAsc(DirectMessageConversation conversation);
    
    /**
     * Count unread messages for a user in a conversation
     */
    @Query("SELECT COUNT(dm) FROM DirectMessage dm WHERE dm.conversation = :conversation AND dm.sender.id != :userId AND dm.isRead = false")
    long countUnreadMessagesInConversation(@Param("conversation") DirectMessageConversation conversation, @Param("userId") Long userId);
    
    /**
     * Mark messages as read in a conversation for a user
     */
    @Modifying
    @Query("UPDATE DirectMessage dm SET dm.isRead = true WHERE dm.conversation = :conversation AND dm.sender.id != :userId AND dm.isRead = false")
    void markMessagesAsReadInConversation(@Param("conversation") DirectMessageConversation conversation, @Param("userId") Long userId);
} 