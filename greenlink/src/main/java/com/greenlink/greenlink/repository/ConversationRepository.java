package com.greenlink.greenlink.repository;

import com.greenlink.greenlink.model.Conversation;
import com.greenlink.greenlink.model.Product;
import com.greenlink.greenlink.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    
    List<Conversation> findBySellerOrderByUpdatedAtDesc(User seller);
    
    List<Conversation> findByBuyerOrderByUpdatedAtDesc(User buyer);
    
    @Query("SELECT c FROM Conversation c WHERE c.seller = ?1 OR c.buyer = ?1 ORDER BY c.updatedAt DESC")
    List<Conversation> findByUser(User user);
    
    Optional<Conversation> findByProductAndBuyer(Product product, User buyer);
    
    @Query("SELECT COUNT(c) FROM Conversation c WHERE c.seller = ?1 AND c.isSellerRead = false")
    long countUnreadConversationsForSeller(User seller);
    
    @Query("SELECT COUNT(c) FROM Conversation c WHERE c.buyer = ?1 AND c.isBuyerRead = false")
    long countUnreadConversationsForBuyer(User buyer);
} 