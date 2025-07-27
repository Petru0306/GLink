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
import com.greenlink.greenlink.service.SystemMessageService;
import com.greenlink.greenlink.service.PointsService;
import com.greenlink.greenlink.model.SystemMessage;
import com.greenlink.greenlink.service.DeliveryConversationService;
import com.greenlink.greenlink.repository.ConversationRepository;
import com.greenlink.greenlink.model.Message;
import com.greenlink.greenlink.repository.MessageRepository;
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
import java.util.stream.Collectors;
import java.util.ArrayList;

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

    @Autowired
    private PointsService pointsService;
    
    @Autowired
    private SystemMessageService systemMessageService;
    
    @Autowired
    private DeliveryConversationService deliveryConversationService;
    
    @Autowired
    private ConversationRepository conversationRepository;
    
    @Autowired
    private MessageRepository messageRepository;
    
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public String getInbox(Model model, Principal principal) {
        try {
            User currentUser = userService.getUserByEmail(principal.getName());
            List<ConversationDto> conversations = messageService.getUserConversations(currentUser);
            List<SystemMessage> systemMessages = systemMessageService.getUnreadMessages(currentUser);
            
            model.addAttribute("conversations", conversations);
            model.addAttribute("systemMessages", systemMessages);
            model.addAttribute("unreadCount", messageService.countUnreadMessages(currentUser));
            model.addAttribute("unreadSystemCount", systemMessageService.countUnreadMessages(currentUser));
            
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
            
            
            if (!conversation.getSeller().getId().equals(currentUser.getId()) && 
                !conversation.getBuyer().getId().equals(currentUser.getId())) {
                return "redirect:/inbox?error=unauthorized";
            }
            
            List<MessageDto> messages = messageService.getConversationMessages(conversationId, currentUser);
            
            
            messageService.markConversationAsRead(conversationId, currentUser);
            
            boolean isCurrentUserSeller = conversation.getSeller().getId().equals(currentUser.getId());
            User otherUser = isCurrentUserSeller ? conversation.getBuyer() : conversation.getSeller();
            
            
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
                
                ProductDto product = productService.getProductById(productId);
                String branch = product.getBranch().toString().toLowerCase();
                return "redirect:/marketplace/" + branch + "/product/" + productId + "?error=" + e.getMessage();
            } catch (Exception ex) {
                
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
            
            
            challengeTrackingService.trackUserAction(currentUser.getId(), "MESSAGE_SENT", null);
            
            
            try {
                pointsService.awardMessageSentPoints(currentUser.getId(), conversationId, 2); 
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error awarding points for message sent: " + e.getMessage());
            }
            
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
            
            
            challengeTrackingService.trackUserAction(currentUser.getId(), "MARKETPLACE_OFFER_MADE", null);
            
            
            try {
                Conversation conversation = messageService.getConversationById(conversationId);
                String productName = conversation.getProduct().getName();
                pointsService.awardOfferMadePoints(currentUser.getId(), conversation.getProduct().getId(), 
                    productName, offerAmount, 5); 
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error awarding points for offer made: " + e.getMessage());
            }
            
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
    
    @PostMapping("/mark-read")
    @PreAuthorize("isAuthenticated()")
    public String markSystemMessageAsRead(@RequestParam Long messageId, Principal principal) {
        try {
            systemMessageService.markAsRead(messageId);
            return "redirect:/inbox?success=Message+marked+as+read";
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error marking message as read", e);
            return "redirect:/inbox?error=" + e.getMessage();
        }
    }
    
    @GetMapping("/conversation/{conversationId}/messages")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<List<MessageDto>> getConversationMessages(
            @PathVariable Long conversationId,
            Principal principal) {
        
        try {
            User currentUser = userService.getUserByEmail(principal.getName());
            List<MessageDto> messages = messageService.getConversationMessages(conversationId, currentUser);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting conversation messages", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/conversation/{conversationId}/read")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<?> markConversationAsReadAjax(
            @PathVariable Long conversationId,
            Principal principal) {
        
        try {
            User currentUser = userService.getUserByEmail(principal.getName());
            messageService.markConversationAsRead(conversationId, currentUser);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error marking conversation as read", e);
            return ResponseEntity.badRequest().build();
        }
    }
    


    /**
     * Show delivery conversations for a user
     */
    @GetMapping("/delivery-conversations")
    @PreAuthorize("isAuthenticated()")
    public String showDeliveryConversations(Model model, Principal principal) {
        try {
            User currentUser = userService.getUserByEmail(principal.getName());
            System.out.println("=== LOOKING FOR DELIVERY CONVERSATIONS ===");
            System.out.println("Current user: " + currentUser.getEmail() + " (ID: " + currentUser.getId() + ")");
            
            
            List<Conversation> allConversations = conversationRepository.findByBuyerOrSellerOrderByUpdatedAtDesc(currentUser, currentUser);
            System.out.println("Found " + allConversations.size() + " total conversations for user");
            
                    
            List<Conversation> deliveryConversations = allConversations.stream()
                    .filter(conv -> conv.getProduct() != null)
                    .collect(Collectors.toList());
            
            System.out.println("Found " + deliveryConversations.size() + " delivery conversations");
            for (Conversation conv : deliveryConversations) {
                System.out.println("  - Conversation ID: " + conv.getId() + ", Product: " + conv.getProduct().getName());
            }
            System.out.println("=== END DELIVERY CONVERSATIONS SEARCH ===");
            
            model.addAttribute("deliveryConversations", deliveryConversations);
            model.addAttribute("currentUser", currentUser);
            
            return "inbox/delivery-conversations";
        } catch (Exception e) {
            System.out.println("ERROR in showDeliveryConversations: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/login";
        }
    }

    /**
     * Show a specific delivery conversation
     */
    @GetMapping("/delivery-conversation/{conversationId}")
    @PreAuthorize("isAuthenticated()")
    public String showDeliveryConversation(@PathVariable Long conversationId, Model model, Principal principal) {
        try {
            User currentUser = userService.getUserByEmail(principal.getName());
            
            Conversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new RuntimeException("Conversation not found"));
            
            // Security check - user must be buyer or seller
            if (!conversation.getBuyer().getId().equals(currentUser.getId()) && 
                !conversation.getSeller().getId().equals(currentUser.getId())) {
                return "redirect:/inbox/delivery-conversations?error=unauthorized";
            }
            
            // Get messages for this conversation
            List<Message> messages = messageRepository.findByConversationOrderBySentAtAsc(conversation);
            
            model.addAttribute("conversation", conversation);
            model.addAttribute("messages", messages);
            model.addAttribute("currentUser", currentUser);
            
            return "inbox/delivery-conversation";
        } catch (Exception e) {
            System.out.println("ERROR in showDeliveryConversation: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/inbox/delivery-conversations?error=not-found";
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
            String conversationIdStr = request.get("conversationId");
            String content = request.get("content");
            
            if (conversationIdStr == null || content == null || content.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Missing conversation ID or message content");
                return ResponseEntity.badRequest().body(response);
            }
            
            Long conversationId = Long.parseLong(conversationIdStr);
            User currentUser = userService.getCurrentUser();
            
            Conversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new RuntimeException("Conversation not found"));
            
            // Security check
            if (!conversation.getBuyer().getId().equals(currentUser.getId()) && 
                !conversation.getSeller().getId().equals(currentUser.getId())) {
                response.put("success", false);
                response.put("message", "Unauthorized");
                return ResponseEntity.badRequest().body(response);
            }
            
            Message message = Message.builder()
                    .conversation(conversation)
                    .sender(currentUser)
                    .content(content.trim())
                    .build();
            
            messageRepository.save(message);
            
            // Track challenge progress for sending first message
            challengeTrackingService.trackUserAction(currentUser.getId(), "MESSAGE_SENT", null);
            
            response.put("success", true);
            response.put("message", "Message sent successfully");
            response.put("messageId", message.getId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("ERROR sending message: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error sending message: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Debug endpoint to check conversations
     */
    @GetMapping("/debug-conversations")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> debugConversations(Principal principal) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentUser = userService.getUserByEmail(principal.getName());
            
            // Get all conversations
            List<Conversation> allConversations = conversationRepository.findAll();
            
            // Get user's conversations
            List<Conversation> userConversations = conversationRepository.findByBuyerOrSellerOrderByUpdatedAtDesc(currentUser, currentUser);
            
            // Get delivery conversations (with products)
            List<Conversation> deliveryConversations = userConversations.stream()
                    .filter(conv -> conv.getProduct() != null)
                    .collect(Collectors.toList());
            
            response.put("totalConversations", allConversations.size());
            response.put("userConversations", userConversations.size());
            response.put("deliveryConversations", deliveryConversations.size());
            response.put("currentUser", currentUser.getEmail());
            
            List<Map<String, Object>> convDetails = new ArrayList<>();
            for (Conversation conv : deliveryConversations) {
                Map<String, Object> convInfo = new HashMap<>();
                convInfo.put("id", conv.getId());
                convInfo.put("productName", conv.getProduct().getName());
                convInfo.put("seller", conv.getSeller().getEmail());
                convInfo.put("buyer", conv.getBuyer().getEmail());
                convInfo.put("createdAt", conv.getCreatedAt());
                convDetails.add(convInfo);
            }
            response.put("conversations", convDetails);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
} 