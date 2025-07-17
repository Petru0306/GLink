package com.greenlink.greenlink.repository;

import com.greenlink.greenlink.model.Friend;
import com.greenlink.greenlink.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {
    List<Friend> findByUser(User user);
    
    @Query("SELECT f FROM Friend f WHERE f.user = :user OR f.friendUser = :user")
    List<Friend> findAllFriendships(User user);
    
    boolean existsByUserAndFriendUser(User user, User friendUser);
}
