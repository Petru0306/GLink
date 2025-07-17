package com.greenlink.greenlink.repository;

import com.greenlink.greenlink.model.FriendRequest;
import com.greenlink.greenlink.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    
    List<FriendRequest> findByReceiverAndStatus(User receiver, String status);
    
    boolean existsBySenderAndReceiverAndStatus(User sender, User receiver, String status);
    
    @Query("SELECT CASE WHEN COUNT(fr) > 0 THEN true ELSE false END FROM FriendRequest fr " +
           "WHERE (fr.sender = :user1 AND fr.receiver = :user2 OR fr.sender = :user2 AND fr.receiver = :user1) " +
           "AND fr.status = 'PENDING'")
    boolean existsPendingRequestBetweenUsers(User user1, User user2);
}
