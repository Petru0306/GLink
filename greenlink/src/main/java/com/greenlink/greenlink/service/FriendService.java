package com.greenlink.greenlink.service;

import com.greenlink.greenlink.model.Friend;
import com.greenlink.greenlink.model.FriendRequest;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.repository.FriendRepository;
import com.greenlink.greenlink.repository.FriendRequestRepository;
import com.greenlink.greenlink.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FriendService {
    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final FriendRequestRepository friendRequestRepository;
    
    @Autowired
    private SystemMessageService systemMessageService;

    public FriendService(FriendRepository friendRepository, UserRepository userRepository, 
                         FriendRequestRepository friendRequestRepository) {
        this.friendRepository = friendRepository;
        this.userRepository = userRepository;
        this.friendRequestRepository = friendRequestRepository;
    }

    @Transactional
    public void addFriend(User user, User friendUser) {
        if (user.equals(friendUser)) {
            throw new IllegalArgumentException("Cannot add yourself as a friend");
        }

        if (friendRepository.existsByUserAndFriendUser(user, friendUser)) {
            throw new IllegalArgumentException("Already friends with this user");
        }

        Friend friendship = new Friend();
        friendship.setUser(user);
        friendship.setFriendUser(friendUser);
        friendRepository.save(friendship);
    }

    @Transactional
    public void removeFriend(User user, User friendUser) {
        List<Friend> friendships = friendRepository.findAllFriendships(user);
        friendships.stream()
                .filter(f -> (f.getUser().equals(user) && f.getFriendUser().equals(friendUser)) ||
                           (f.getUser().equals(friendUser) && f.getFriendUser().equals(user)))
                .forEach(friendRepository::delete);
    }

    public List<User> getFriendsList(User user) {
        return friendRepository.findAllFriendships(user).stream()
                .map(f -> f.getUser().equals(user) ? f.getFriendUser() : f.getUser())
                .collect(Collectors.toList());
    }

    public List<User> searchUsers(String query) {
        return userRepository.findByEmailContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(query, query, query);
    }
    
    // Friend Request Methods
    @Transactional
    public void sendFriendRequest(User sender, User receiver) {
        if (sender.equals(receiver)) {
            throw new IllegalArgumentException("Cannot send friend request to yourself");
        }
        
        // Check if they are already friends
        if (friendRepository.existsByUserAndFriendUser(sender, receiver) || 
            friendRepository.existsByUserAndFriendUser(receiver, sender)) {
            throw new IllegalArgumentException("Already friends with this user");
        }
        
        // Check if there is already a pending request
        if (friendRequestRepository.existsPendingRequestBetweenUsers(sender, receiver)) {
            throw new IllegalArgumentException("Friend request already pending");
        }
        
        FriendRequest request = new FriendRequest();
        request.setSender(sender);
        request.setReceiver(receiver);
        friendRequestRepository.save(request);
        
        // Send notification to receiver
        String notificationContent = sender.getFirstName() + " " + sender.getLastName() + " wants to be your friend!";
        systemMessageService.sendSystemMessage(receiver, notificationContent, "FRIEND_REQUEST");
    }
    
    @Transactional
    public void acceptFriendRequest(Long requestId, User receiver) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Friend request not found"));
                
        if (!request.getReceiver().equals(receiver)) {
            throw new IllegalArgumentException("You cannot accept this request");
        }
        
        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalArgumentException("This request has already been processed");
        }
        
        // Update request status
        request.setStatus("ACCEPTED");
        request.setRespondedAt(LocalDateTime.now());
        friendRequestRepository.save(request);
        
        // Create friend relationship
        addFriend(request.getReceiver(), request.getSender());
    }
    
    @Transactional
    public void declineFriendRequest(Long requestId, User receiver) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Friend request not found"));
                
        if (!request.getReceiver().equals(receiver)) {
            throw new IllegalArgumentException("You cannot decline this request");
        }
        
        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalArgumentException("This request has already been processed");
        }
        
        // Update request status
        request.setStatus("DECLINED");
        request.setRespondedAt(LocalDateTime.now());
        friendRequestRepository.save(request);
    }
    
    public List<FriendRequest> getPendingFriendRequests(User user) {
        return friendRequestRepository.findByReceiverAndStatus(user, "PENDING");
    }
    
    public boolean hasPendingRequestTo(User sender, User receiver) {
        return friendRequestRepository.existsBySenderAndReceiverAndStatus(sender, receiver, "PENDING");
    }
}
