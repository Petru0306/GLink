package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.dto.DirectMessageConversationDto;
import com.greenlink.greenlink.dto.DirectMessageDto;
import com.greenlink.greenlink.model.DirectMessageConversation;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.service.DirectMessageService;
import com.greenlink.greenlink.service.FriendService;
import com.greenlink.greenlink.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.ui.Model;
import com.greenlink.greenlink.model.Conversation;
import com.greenlink.greenlink.repository.ConversationRepository;
import com.greenlink.greenlink.service.DeliveryConversationService;
import com.greenlink.greenlink.model.Message;
import com.greenlink.greenlink.repository.MessageRepository;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/dm")
public class DirectMessageController {

    private static final Logger logger = Logger.getLogger(DirectMessageController.class.getName());

    @Autowired
    private DirectMessageService directMessageService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private FriendService friendService;
    
    @Autowired
    private DeliveryConversationService deliveryConversationService;
    
    @Autowired
    private ConversationRepository conversationRepository;
    
    @Autowired
    private MessageRepository messageRepository;
    
    @GetMapping("/conversations")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<List<DirectMessageConversationDto>> getUserConversations(Principal principal) {
        try {
            User currentUser = userService.getUserByEmail(principal.getName());
            List<DirectMessageConversationDto> conversations = directMessageService.getUserConversations(currentUser);
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting user DM conversations", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/conversation/{userId}")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<?> startConversation(@PathVariable Long userId, Principal principal) {
        try {
            logger.info("Starting DM conversation for user ID: " + userId + " by principal: " + principal.getName());
            
            User currentUser = userService.getUserByEmail(principal.getName());
            logger.info("Current user: " + currentUser.getEmail() + " (ID: " + currentUser.getId() + ")");
            
            User targetUser = userService.getUserById(userId);
            logger.info("Target user found: " + (targetUser != null ? targetUser.getEmail() : "null") + " (ID: " + userId + ")");
            
            if (targetUser == null) {
                logger.warning("Target user not found for ID: " + userId);
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }
            
            boolean canSendMessage = directMessageService.canSendMessage(currentUser, targetUser);
            logger.info("Can send message: " + canSendMessage);
            
            if (!canSendMessage) {
                logger.warning("User " + currentUser.getId() + " cannot send message to user " + targetUser.getId());
                return ResponseEntity.badRequest().body(Map.of("error", "You can only send messages to friends"));
            }
            
            DirectMessageConversation conversation = directMessageService.getOrCreateConversation(currentUser, targetUser);
            logger.info("Created/found conversation: " + conversation.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("conversationId", conversation.getId());
            response.put("otherUser", targetUser.getFirstName() + " " + targetUser.getLastName());
            response.put("currentUserId", currentUser.getId());
            
            logger.info("DM conversation started successfully: " + response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error starting DM conversation for user ID: " + userId, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/conversation/{conversationId}/messages")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<List<DirectMessageDto>> getConversationMessages(
            @PathVariable Long conversationId,
            Principal principal) {
        
        try {
            User currentUser = userService.getUserByEmail(principal.getName());
            List<DirectMessageDto> messages = directMessageService.getConversationMessages(conversationId, currentUser);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting DM conversation messages", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/conversation/{conversationId}/message")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<?> sendMessage(
            @PathVariable Long conversationId,
            @RequestParam String content,
            Principal principal) {
        
        try {
            User currentUser = userService.getUserByEmail(principal.getName());
            
            DirectMessageDto message = directMessageService.sendMessage(conversationId, currentUser, content);
            
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error sending DM", e);
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/conversation/{conversationId}/read")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<?> markConversationAsRead(
            @PathVariable Long conversationId,
            Principal principal) {
        
        try {
            User currentUser = userService.getUserByEmail(principal.getName());
            directMessageService.markConversationAsRead(conversationId, currentUser);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error marking DM conversation as read", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<Map<String, Long>> getUnreadCount(Principal principal) {
        try {
            User currentUser = userService.getUserByEmail(principal.getName());
            long count = directMessageService.countUnreadMessages(currentUser);
            
            Map<String, Long> response = new HashMap<>();
            response.put("count", count);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting DM unread count", e);
            return ResponseEntity.ok(Map.of("count", 0L));
        }
    }
    
    @GetMapping("/can-message/{userId}")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> canSendMessage(@PathVariable Long userId, Principal principal) {
        try {
            User currentUser = userService.getUserByEmail(principal.getName());
            User targetUser = userService.getUserById(userId);
            
            if (targetUser == null) {
                return ResponseEntity.ok(Map.of("canMessage", false));
            }
            
            boolean canMessage = directMessageService.canSendMessage(currentUser, targetUser);
            return ResponseEntity.ok(Map.of("canMessage", canMessage));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error checking if can send message", e);
            return ResponseEntity.ok(Map.of("canMessage", false));
        }
    }

    /**
     * Show delivery conversations for a user
     */
    @GetMapping("/delivery-conversations")
    @PreAuthorize("isAuthenticated()")
    public String showDeliveryConversations(Model model) {
        try {
            User currentUser = userService.getCurrentUser();
            
            // Get active delivery conversations
            List<Conversation> deliveryConversations = deliveryConversationService.getActiveDeliveryConversationsForUser(currentUser);
            
            model.addAttribute("deliveryConversations", deliveryConversations);
            model.addAttribute("currentUser", currentUser);
            
            return "inbox/delivery-conversations";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }
    
    /**
     * Show a specific delivery conversation
     */
    @GetMapping("/delivery-conversation/{conversationId}")
    @PreAuthorize("isAuthenticated()")
    public String showDeliveryConversation(@PathVariable Long conversationId, Model model) {
        try {
            User currentUser = userService.getCurrentUser();
            Conversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new RuntimeException("Conversation not found"));
            
            // Check if user is part of this conversation
            if (!conversation.getBuyer().getId().equals(currentUser.getId()) && 
                !conversation.getSeller().getId().equals(currentUser.getId())) {
                return "redirect:/inbox";
            }
            
            // Mark messages as read
            if (conversation.getBuyer().getId().equals(currentUser.getId())) {
                conversation.setBuyerRead(true);
            } else {
                conversation.setSellerRead(true);
            }
            conversationRepository.save(conversation);
            
            model.addAttribute("conversation", conversation);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("otherUser", conversation.getBuyer().getId().equals(currentUser.getId()) ? 
                conversation.getSeller() : conversation.getBuyer());
            
            return "inbox/delivery-conversation";
        } catch (Exception e) {
            return "redirect:/inbox";
        }
    }

    /**
     * Send a message in a delivery conversation
     */
    @PostMapping("/send-message")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendMessage(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long conversationId = Long.parseLong(request.get("conversationId"));
            String content = request.get("content");
            
            User currentUser = userService.getCurrentUser();
            Conversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new RuntimeException("Conversation not found"));
            
            // Check if user is part of this conversation
            if (!conversation.getBuyer().getId().equals(currentUser.getId()) && 
                !conversation.getSeller().getId().equals(currentUser.getId())) {
                response.put("success", false);
                response.put("message", "You are not part of this conversation");
                return ResponseEntity.ok(response);
            }
            
            // Create and save message
            Message message = Message.builder()
                    .conversation(conversation)
                    .sender(currentUser)
                    .content(content)
                    .build();
            
            messageRepository.save(message);
            
            // Update conversation timestamps
            conversation.setUpdatedAt(LocalDateTime.now());
            conversationRepository.save(conversation);
            
            response.put("success", true);
            response.put("message", "Message sent successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error sending message: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * Update delivery status
     */
    @PostMapping("/update-delivery-status")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateDeliveryStatus(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long conversationId = Long.parseLong(request.get("conversationId"));
            String status = request.get("status");
            
            User currentUser = userService.getCurrentUser();
            Conversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new RuntimeException("Conversation not found"));
            
            // Check if user is the seller
            if (!conversation.getSeller().getId().equals(currentUser.getId())) {
                response.put("success", false);
                response.put("message", "Only the seller can update delivery status");
                return ResponseEntity.ok(response);
            }
            
            // Update delivery status
            deliveryConversationService.updateDeliveryStatus(conversationId, status);
            
            response.put("success", true);
            response.put("message", "Delivery status updated successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating status: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * Complete delivery
     */
    @PostMapping("/complete-delivery")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> completeDelivery(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long conversationId = Long.parseLong(request.get("conversationId"));
            
            User currentUser = userService.getCurrentUser();
            Conversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new RuntimeException("Conversation not found"));
            
            // Check if user is the seller
            if (!conversation.getSeller().getId().equals(currentUser.getId())) {
                response.put("success", false);
                response.put("message", "Only the seller can complete delivery");
                return ResponseEntity.ok(response);
            }
            
            // Complete delivery
            deliveryConversationService.completeDeliveryConversation(conversationId);
            
            response.put("success", true);
            response.put("message", "Delivery completed successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error completing delivery: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
} 