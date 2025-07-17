package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.model.FriendRequest;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.repository.UserRepository;
import com.greenlink.greenlink.service.FriendService;
import com.greenlink.greenlink.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/friends")
public class FriendController {
    private final FriendService friendService;
    private final UserService userService;
    private final UserRepository userRepository;

    public FriendController(FriendService friendService, UserService userService, UserRepository userRepository) {
        this.friendService = friendService;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String friendsPage(@AuthenticationPrincipal User user, Model model) {
        List<User> friends = friendService.getFriendsList(user);
        List<FriendRequest> friendRequests = friendService.getPendingFriendRequests(user);
        
        model.addAttribute("friends", friends);
        model.addAttribute("friendRequests", friendRequests);
        return "friends";
    }

    @GetMapping("/search")
    public String searchUsers(@RequestParam(name = "q") String query, @AuthenticationPrincipal User currentUser, Model model) {
        List<User> friends = friendService.getFriendsList(currentUser);
        List<User> searchResults = userRepository.searchUsers(query, currentUser.getId());
        
        // Mark which users are already friends and which have pending requests
        for (User user : searchResults) {
            if (friends.stream().anyMatch(f -> f.getId().equals(user.getId()))) {
                user.setAdditionalProperty("isFriend", true);
            } else {
                user.setAdditionalProperty("isFriend", false);
                user.setAdditionalProperty("requestSent", friendService.hasPendingRequestTo(currentUser, user));
            }
        }
        
        model.addAttribute("searchResults", searchResults);
        model.addAttribute("friends", friends);
        return "friends";
    }

    @PostMapping("/send-request")
    public String sendFriendRequest(@RequestParam Long userId, @AuthenticationPrincipal User currentUser) {
        try {
            User receiverUser = userService.getUserById(userId);
            friendService.sendFriendRequest(currentUser, receiverUser);
            return "redirect:/friends?success=request-sent";
        } catch (Exception e) {
            return "redirect:/friends?error=" + e.getMessage();
        }
    }

    @PostMapping("/remove")
    public String removeFriend(@RequestParam Long userId, @AuthenticationPrincipal User currentUser) {
        try {
            User friendUser = userService.getUserById(userId);
            friendService.removeFriend(currentUser, friendUser);
            return "redirect:/friends?success=friend-removed";
        } catch (Exception e) {
            return "redirect:/friends?error=" + e.getMessage();
        }
    }
    
    @PostMapping("/accept")
    public String acceptFriendRequest(@RequestParam Long requestId, @AuthenticationPrincipal User currentUser) {
        try {
            friendService.acceptFriendRequest(requestId, currentUser);
            return "redirect:/friends?success=request-accepted";
        } catch (Exception e) {
            return "redirect:/friends?error=" + e.getMessage();
        }
    }
    
    @PostMapping("/decline")
    public String declineFriendRequest(@RequestParam Long requestId, @AuthenticationPrincipal User currentUser) {
        try {
            friendService.declineFriendRequest(requestId, currentUser);
            return "redirect:/friends?success=request-declined";
        } catch (Exception e) {
            return "redirect:/friends?error=" + e.getMessage();
        }
    }

    @GetMapping("/profile/{userId}")
    public String viewProfile(@PathVariable Long userId, Model model) {
        User user = userService.getUserById(userId);
        model.addAttribute("profileUser", user);
        return "friends/profile";
    }
}
