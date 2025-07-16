package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.dto.ConversationDto;
import com.greenlink.greenlink.dto.MessageDto;
import com.greenlink.greenlink.dto.ProductDto;
import com.greenlink.greenlink.model.Conversation;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.service.MessageService;
import com.greenlink.greenlink.service.ProductService;
import com.greenlink.greenlink.service.UserService;
import com.greenlink.greenlink.service.ChallengeTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
@RequestMapping("/inbox")
public class MessageController {

    private static final Logger logger = Logger.getLogger(MessageController.class.getName());

    @Autowired
    private MessageService messageService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ProductService productService;

    @Autowired
    private ChallengeTrackingService challengeTrackingService;
    
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public String getInbox(Model model, Principal principal) {
        try {
            User currentUser = userService.getUserByEmail(principal.getName());
            List<ConversationDto> conversations = messageService.getUserConversations(currentUser);
            
            model.addAttribute("conversations", conversations);
            model.addAttribute("unreadCount", messageService.countUnreadMessages(currentUser));
            
            return "inbox/inbox";
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading inbox", e);
            model.addAttribute("error", "An error occurred while loading your inbox: " + e.getMessage());
            return "redirect:/?error=Error+loading+inbox";
        }
    }
    
    @GetMapping("/conversation/{conversationId}")
    @PreAuthorize("isAuthenticated()")
    public String getConversation(@PathVariable Long conversationId, Model model, Principal principal) {
        try {
            User currentUser = userService.getUserByEmail(principal.getName());
            
            Conversation conversation = messageService.getConversationById(conversationId);
            
            // Security check
            if (!conversation.getSeller().getId().equals(currentUser.getId()) && 
                !conversation.getBuyer().getId().equals(currentUser.getId())) {
                return "redirect:/inbox?error=unauthorized";
            }
            
            List<MessageDto> messages = messageService.getConversationMessages(conversationId, currentUser);
            
            // Mark conversation as read
            messageService.markConversationAsRead(conversationId, currentUser);
            
            boolean isCurrentUserSeller = conversation.getSeller().getId().equals(currentUser.getId());
            User otherUser = isCurrentUserSeller ? conversation.getBuyer() : conversation.getSeller();
            
            // Get product with negotiated price if applicable
            ProductDto product = productService.getProductById(conversation.getProduct().getId(), currentUser);
            
            model.addAttribute("conversation", conversation);
            model.addAttribute("messages", messages);
            model.addAttribute("product", product);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("otherUser", otherUser);
            model.addAttribute("isCurrentUserSeller", isCurrentUserSeller);
            
            return "inbox/conversation";
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading conversation", e);
            return "redirect:/inbox?error=" + e.getMessage();
        }
    }
    
    @GetMapping("/start/{productId}")
    @PreAuthorize("isAuthenticated()")
    public String startConversation(@PathVariable Long productId, Model model, Principal principal) {
        try {
            User currentUser = userService.getUserByEmail(principal.getName());
            
            Conversation conversation = messageService.getOrCreateConversation(productId, currentUser);
            return "redirect:/inbox/conversation/" + conversation.getId();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error starting conversation", e);
            try {
                // Get the product to determine the branch for proper redirection
                ProductDto product = productService.getProductById(productId);
                String branch = product.getBranch().toString().toLowerCase();
                return "redirect:/marketplace/" + branch + "/product/" + productId + "?error=" + e.getMessage();
            } catch (Exception ex) {
                // If we can't get the product, redirect to marketplace
                return "redirect:/marketplace?error=" + e.getMessage();
            }
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
            
            MessageDto message = messageService.sendMessage(conversationId, currentUser, content);
            
            // Track message sending for challenges
            challengeTrackingService.trackUserAction(currentUser.getId(), "MESSAGE_SENT", null);
            
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error sending message", e);
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/conversation/{conversationId}/offer")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<?> makeOffer(
            @PathVariable Long conversationId,
            @RequestParam Double offerAmount,
            Principal principal) {
        
        try {
            User currentUser = userService.getUserByEmail(principal.getName());
            
            MessageDto message = messageService.makeOffer(conversationId, currentUser, offerAmount);
            
            // Track marketplace offer for challenges
            challengeTrackingService.trackUserAction(currentUser.getId(), "MARKETPLACE_OFFER_MADE", null);
            
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error making offer", e);
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/message/{messageId}/respond")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<?> respondToOffer(
            @PathVariable Long messageId,
            @RequestParam String action,
            @RequestParam(required = false) Double counterOfferAmount,
            Principal principal) {
        
        try {
            User currentUser = userService.getUserByEmail(principal.getName());
            
            MessageDto message = messageService.respondToOffer(messageId, currentUser, action, counterOfferAmount);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error responding to offer", e);
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<Map<String, Long>> getUnreadCount(Principal principal) {
        try {
            User currentUser = userService.getUserByEmail(principal.getName());
            long count = messageService.countUnreadMessages(currentUser);
            
            Map<String, Long> response = new HashMap<>();
            response.put("count", count);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting unread count", e);
            return ResponseEntity.ok(Map.of("count", 0L));
        }
    }
} 