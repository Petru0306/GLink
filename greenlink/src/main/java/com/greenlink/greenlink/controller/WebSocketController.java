package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.dto.ChatMessageDto;
import com.greenlink.greenlink.dto.MessageDto;
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
            Long conversationId = payload.get("conversationId") != null ? 
                    Long.parseLong(payload.get("conversationId").toString()) : null;
                    
            if (conversationId != null) {
                // Add user to conversation room
                var sessionAttributes = headerAccessor.getSessionAttributes();
                if (sessionAttributes != null) {
                    sessionAttributes.put("conversationId", conversationId);
                }
                
                // Send confirmation to user
                Map<String, Object> response = new HashMap<>();
                response.put("type", "JOINED");
                response.put("conversationId", conversationId);
                response.put("timestamp", System.currentTimeMillis());
                
                messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/chat.joined", response);
                logger.info("User " + principal.getName() + " joined conversation: " + conversationId);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error handling join message", e);
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
            
            // If client provided a tempId, include it in response for tracking
            if (chatMessage.getTempId() != null) {
                responseMessage.setTempId(chatMessage.getTempId());
            }
            
            // Broadcast to conversation topic
            messagingTemplate.convertAndSend(
                    "/topic/conversation." + chatMessage.getConversationId(),
                    responseMessage
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
            
            // If client provided a tempId, include it in response for tracking
            if (chatMessage.getTempId() != null && savedMessage != null) {
                // Convert back to ChatMessageDto for broadcasting (only for counter offers)
                ChatMessageDto responseMessage = ChatMessageDto.fromMessageDto(savedMessage);
                responseMessage.setTempId(chatMessage.getTempId());
                
                // Broadcast the counter offer message
                messagingTemplate.convertAndSend(
                        "/topic/conversation." + chatMessage.getConversationId(),
                        responseMessage
                );
            }
            
            // Always send a status update for the original offer (for accept/reject/counter)
            ChatMessageDto statusUpdate = new ChatMessageDto();
            statusUpdate.setMessageId(chatMessage.getMessageId());
            statusUpdate.setConversationId(chatMessage.getConversationId());
            statusUpdate.setOfferStatus(chatMessage.getOfferStatus());
            statusUpdate.setOffer(true);
            // Ensure content is null to indicate this is a status update only
            statusUpdate.setContent(null);
            
            // Send status update
            messagingTemplate.convertAndSend(
                    "/topic/conversation." + chatMessage.getConversationId(),
                    statusUpdate
            );
            
            logger.info("Status update sent via WebSocket for message " + chatMessage.getMessageId() + " with status: " + chatMessage.getOfferStatus());
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in WebSocket offer response handling", e);
        }
    }
} 