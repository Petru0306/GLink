package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.dto.ChatMessageDto;
import com.greenlink.greenlink.dto.MessageDto;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.service.MessageService;
import com.greenlink.greenlink.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

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
    
    /**
     * Handle ping messages to confirm WebSocket connectivity
     */
    @MessageMapping("/chat.ping")
    public void handlePing(@Payload Map<String, Object> payload) {
        try {
            Long conversationId = payload.get("conversationId") != null ? 
                    Long.parseLong(payload.get("conversationId").toString()) : null;
                    
            if (conversationId != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("type", "PING");
                response.put("timestamp", System.currentTimeMillis());
                
                // Echo back to conversation topic
                messagingTemplate.convertAndSend("/topic/conversation." + conversationId, response);
                logger.info("Ping response sent to conversation: " + conversationId);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error handling ping message", e);
        }
    }

    @MessageMapping("/chat.sendMessage")
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
            
            // Convert back to ChatMessageDto for broadcasting
            ChatMessageDto responseMessage = ChatMessageDto.fromMessageDto(savedMessage);
            
            // If client provided a tempId, include it in response for tracking
            if (chatMessage.getTempId() != null) {
                responseMessage.setTempId(chatMessage.getTempId());
            }
            
            // We need to broadcast both the response message (which is a new message)
            // and a status update for the original offer
            messagingTemplate.convertAndSend(
                    "/topic/conversation." + chatMessage.getConversationId(),
                    responseMessage
            );
            
            // Create a status update message for the original offer
            ChatMessageDto statusUpdate = new ChatMessageDto();
            statusUpdate.setMessageId(chatMessage.getMessageId());
            statusUpdate.setConversationId(chatMessage.getConversationId());
            statusUpdate.setOfferStatus(chatMessage.getOfferStatus());
            statusUpdate.setOffer(true);
            
            // Send status update separately
            messagingTemplate.convertAndSend(
                    "/topic/conversation." + chatMessage.getConversationId(),
                    statusUpdate
            );
            
            logger.info("Offer response sent via WebSocket: " + responseMessage.getOfferStatus());
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in WebSocket offer response handling", e);
        }
    }
} 