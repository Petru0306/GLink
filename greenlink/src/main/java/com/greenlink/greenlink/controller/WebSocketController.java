package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.dto.ChatMessageDto;
import com.greenlink.greenlink.dto.MessageDto;
import com.greenlink.greenlink.dto.OfferEvent;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.service.MessageService;
import com.greenlink.greenlink.service.UserService;
import com.greenlink.greenlink.service.ChallengeTrackingService;
import com.greenlink.greenlink.service.PointsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
public class WebSocketController {

    private static final Logger logger = Logger.getLogger(WebSocketController.class.getName());

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ChallengeTrackingService challengeTrackingService;

    @Autowired
    private PointsService pointsService;
    
    /**
     * Handle ping messages to confirm WebSocket connectivity
     */
    @MessageMapping("/chat.ping")
    public void handlePing(@Payload Map<String, Object> payload, Principal principal) {
        try {
            Long conversationId = payload.get("conversationId") != null ? 
                    Long.parseLong(payload.get("conversationId").toString()) : null;
                    
            if (conversationId != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("type", "PING");
                response.put("timestamp", System.currentTimeMillis());
                response.put("conversationId", conversationId);
                
                // Echo back to conversation topic
                messagingTemplate.convertAndSend("/topic/conversation." + conversationId, response);
                logger.info("Ping response sent to conversation: " + conversationId + " by user: " + principal.getName());
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error handling ping message", e);
        }
    }
    
    /**
     * Handle user joining a conversation
     */
    @MessageMapping("/chat.join")
    public void joinConversation(@Payload Map<String, Object> payload, Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        try {
            Long conversationId = Long.parseLong(payload.get("conversationId").toString());
            
            // Mark conversation as read for the joining user
            User currentUser = userService.getUserByEmail(principal.getName());
            messageService.markConversationAsRead(conversationId, currentUser);
            
            // Send join confirmation to user
            Map<String, Object> joinConfirmation = new HashMap<>();
            joinConfirmation.put("type", "JOIN_CONFIRMATION");
            joinConfirmation.put("conversationId", conversationId);
            joinConfirmation.put("userId", currentUser.getId());
            joinConfirmation.put("timestamp", System.currentTimeMillis());
            
            messagingTemplate.convertAndSendToUser(
                currentUser.getId().toString(),
                "/queue/chat.joined",
                joinConfirmation
            );
            
            logger.info("User " + currentUser.getId() + " joined conversation: " + conversationId);
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error handling join conversation", e);
        }
    }

    @MessageMapping("/chat.sendMessage")
    @Transactional
    public void sendMessage(@Payload ChatMessageDto chatMessage, Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        try {
            // Get user from principal
            User currentUser = userService.getUserByEmail(principal.getName());
            
            // Send message using existing service
            MessageDto savedMessage = messageService.sendMessage(
                    chatMessage.getConversationId(), 
                    currentUser, 
                    chatMessage.getContent()
            );
            
            // Convert back to ChatMessageDto for broadcasting
            ChatMessageDto responseMessage = ChatMessageDto.fromMessageDto(savedMessage);
            
            // If client provided a tempId, include it in response for tracking
            if (chatMessage.getTempId() != null) {
                responseMessage.setTempId(chatMessage.getTempId());
            }
            
            // Broadcast to conversation topic - this will send to all subscribers including the sender
            messagingTemplate.convertAndSend(
                    "/topic/conversation." + chatMessage.getConversationId(), 
                    responseMessage
            );
            
            logger.info("Message sent via WebSocket: " + responseMessage.getContent());
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in WebSocket message handling", e);
        }
    }
    
    @MessageMapping("/chat.offer")
    @Transactional
    public void sendOffer(@Payload ChatMessageDto chatMessage, Principal principal) {
        try {
            // Get user from principal
            User currentUser = userService.getUserByEmail(principal.getName());
            
            // Send offer using existing service
            MessageDto savedMessage = messageService.makeOffer(
                    chatMessage.getConversationId(),
                    currentUser,
                    chatMessage.getOfferAmount()
            );
            
            // Track marketplace offer for challenges
            challengeTrackingService.trackUserAction(currentUser.getId(), "MARKETPLACE_OFFER_MADE", null);
            
            // Award points for making offer
            try {
                String productName = "Product"; // We'll get this from the conversation if needed
                pointsService.awardOfferMadePoints(currentUser.getId(), 1L, productName, chatMessage.getOfferAmount(), 5);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error awarding points for offer made: " + e.getMessage());
            }
            
            // Convert back to ChatMessageDto for broadcasting
            ChatMessageDto responseMessage = ChatMessageDto.fromMessageDto(savedMessage);
            responseMessage.setEventType("OFFER_CREATED");
            
            // If client provided a tempId, include it in response for tracking
            if (chatMessage.getTempId() != null) {
                responseMessage.setTempId(chatMessage.getTempId());
            }
            
            // Broadcast to conversation topic
            messagingTemplate.convertAndSend(
                    "/topic/conversation." + chatMessage.getConversationId(),
                    responseMessage
            );
            
            // Also send to specific offer topic for real-time updates
            messagingTemplate.convertAndSend(
                    "/topic/offer." + savedMessage.getId(),
                    createOfferEvent(OfferEvent.EventType.OFFER_CREATED, savedMessage, currentUser, null)
            );
            
            logger.info("Offer sent via WebSocket: " + responseMessage.getOfferAmount());
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in WebSocket offer handling", e);
        }
    }
    
    @MessageMapping("/chat.respondOffer")
    @Transactional
    public void respondToOffer(@Payload ChatMessageDto chatMessage, Principal principal) {
        try {
            // Get user from principal
            User currentUser = userService.getUserByEmail(principal.getName());
            
            // Respond to offer using existing service
            MessageDto savedMessage = messageService.respondToOffer(
                    chatMessage.getMessageId(),
                    currentUser,
                    chatMessage.getOfferStatus(),
                    chatMessage.getOfferAmount()
            );
            
            // Get the original offer message for status update
            MessageDto originalMessage = messageService.getMessageById(chatMessage.getMessageId(), currentUser);
            
            // Create status update for the original offer
            ChatMessageDto statusUpdate = new ChatMessageDto();
            statusUpdate.setMessageId(chatMessage.getMessageId());
            statusUpdate.setConversationId(chatMessage.getConversationId());
            statusUpdate.setOfferStatus(chatMessage.getOfferStatus());
            statusUpdate.setOfferAmount(originalMessage.getOfferAmount());
            statusUpdate.setOffer(true);
            statusUpdate.setEventType("OFFER_UPDATED");
            // Ensure content is null to indicate this is a status update only
            statusUpdate.setContent(null);
            
            // Send status update to conversation topic
            messagingTemplate.convertAndSend(
                    "/topic/conversation." + chatMessage.getConversationId(),
                    statusUpdate
            );
            
            // Send specific offer event
            OfferEvent.EventType eventType = determineEventType(chatMessage.getOfferStatus());
            messagingTemplate.convertAndSend(
                    "/topic/offer." + chatMessage.getMessageId(),
                    createOfferEvent(eventType, originalMessage, currentUser, chatMessage.getOfferStatus())
            );
            
            // If client provided a tempId, include it in response for tracking (only for counter offers)
            if (chatMessage.getTempId() != null && savedMessage != null) {
                // Convert back to ChatMessageDto for broadcasting (only for counter offers)
                ChatMessageDto responseMessage = ChatMessageDto.fromMessageDto(savedMessage);
                responseMessage.setTempId(chatMessage.getTempId());
                responseMessage.setEventType("COUNTER_OFFER_CREATED");
                
                // Broadcast the counter offer message
                messagingTemplate.convertAndSend(
                        "/topic/conversation." + chatMessage.getConversationId(),
                        responseMessage
                );
                
                // Send counter offer event
                messagingTemplate.convertAndSend(
                        "/topic/offer." + savedMessage.getId(),
                        createOfferEvent(OfferEvent.EventType.COUNTER_OFFER_CREATED, savedMessage, currentUser, null)
                );
            }
            
            // If offer was accepted, send a price update notification to all participants
            if ("ACCEPT".equalsIgnoreCase(chatMessage.getOfferStatus()) && chatMessage.getOfferAmount() != null) {
                ChatMessageDto priceUpdate = new ChatMessageDto();
                priceUpdate.setConversationId(chatMessage.getConversationId());
                priceUpdate.setOfferAmount(chatMessage.getOfferAmount());
                priceUpdate.setContent("PRICE_UPDATE"); // Use content field to indicate this is a price update
                priceUpdate.setOfferStatus("PRICE_UPDATED"); // Use offerStatus to indicate price update
                priceUpdate.setEventType("PRICE_UPDATED");
                
                // Send price update to all participants
                messagingTemplate.convertAndSend(
                        "/topic/conversation." + chatMessage.getConversationId(),
                        priceUpdate
                );
                
                // Send price update event
                messagingTemplate.convertAndSend(
                        "/topic/offer." + chatMessage.getMessageId(),
                        createOfferEvent(OfferEvent.EventType.PRICE_UPDATED, originalMessage, currentUser, null)
                );
                
                logger.info("Price update notification sent for conversation " + chatMessage.getConversationId() + 
                           " with new price: " + chatMessage.getOfferAmount() + " RON");
            }
            
            logger.info("Status update sent via WebSocket for message " + chatMessage.getMessageId() + " with status: " + chatMessage.getOfferStatus());
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in WebSocket offer response handling", e);
        }
    }
    
    /**
     * Create offer event for real-time updates
     */
    private OfferEvent createOfferEvent(OfferEvent.EventType eventType, MessageDto message, User user, String action) {
        return OfferEvent.builder()
                .eventType(eventType)
                .messageId(message.getId())
                .conversationId(message.getConversationId())
                .offerStatus(message.getOfferStatus())
                .offerAmount(message.getOfferAmount())
                .senderId(message.getSenderId())
                .senderName(message.getSenderName())
                .responderId(user.getId())
                .responderName(user.getFirstName() + " " + user.getLastName())
                .timestamp(java.time.LocalDateTime.now())
                .version(message.getVersion())
                .offerExpiresAt(message.getOfferExpiresAt())
                .counterOfferMessageId(message.getCounterOfferMessageId())
                .originalOfferMessageId(message.getOriginalOfferMessageId())
                .isExpired(message.isExpired())
                .canBeActedUpon(message.isCanBeActedUpon())
                .isCounterOffer(message.isCounterOffer())
                .isOriginalOffer(message.isOriginalOffer())
                .build();
    }
    
    /**
     * Determine event type based on action
     */
    private OfferEvent.EventType determineEventType(String action) {
        switch (action.toUpperCase()) {
            case "ACCEPT":
                return OfferEvent.EventType.OFFER_ACCEPTED;
            case "REJECT":
                return OfferEvent.EventType.OFFER_REJECTED;
            case "COUNTER":
                return OfferEvent.EventType.COUNTER_OFFER_CREATED;
            default:
                return OfferEvent.EventType.OFFER_UPDATED;
        }
    }
} 