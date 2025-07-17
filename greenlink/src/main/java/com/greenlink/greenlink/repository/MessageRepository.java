package com.greenlink.greenlink.repository;

import com.greenlink.greenlink.model.Conversation;
import com.greenlink.greenlink.model.Message;
import com.greenlink.greenlink.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    List<Message> findByConversationOrderBySentAtAsc(Conversation conversation);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation = ?1 AND m.sender != ?2 AND m.isRead = false")
    long countUnreadMessagesInConversation(Conversation conversation, User currentUser);
    
    @Query("SELECT COUNT(m) FROM Message m JOIN m.conversation c WHERE (c.buyer = ?1 OR c.seller = ?1) AND m.sender != ?1 AND m.isRead = false")
    long countTotalUnreadMessages(User user);
} 