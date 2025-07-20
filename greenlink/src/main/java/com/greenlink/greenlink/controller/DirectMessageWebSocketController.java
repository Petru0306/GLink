package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.dto.DirectMessageDto;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.service.DirectMessageService;
import com.greenlink.greenlink.service.UserService;
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
public class DirectMessageWebSocketController {

    private static final Logger logger = Logger.getLogger(DirectMessageWebSocketController.class.getName());

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private DirectMessageService directMessageService;
    
    /**
     * Handle ping messages to confirm WebSocket connectivity for DM
     */
    @MessageMapping("/dm.ping")
    public void handlePing(@Payload Map<String, Object> payload, Principal principal) {
        try {
            Long conversationId = payload.get("conversationId") != null ? 
                    Long.parseLong(payload.get("conversationId").toString()) : null;
                    
            if (conversationId != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("type", "PING");
                response.put("timestamp", System.currentTimeMillis());
                response.put("conversationId", conversationId);
                
                // Echo back to DM conversation topic
                messagingTemplate.convertAndSend("/topic/dm." + conversationId, response);
                logger.info("DM Ping response sent to conversation: " + conversationId + " by user: " + principal.getName());
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error handling DM ping message", e);
        }
    }
    
    /**
     * Handle user joining a DM conversation
     */
    @MessageMapping("/dm.join")
    public void joinConversation(@Payload Map<String, Object> payload, Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        try {
            Long conversationId = payload.get("conversationId") != null ? 
                    Long.parseLong(payload.get("conversationId").toString()) : null;
                    
            if (conversationId != null) {
                // Add user to conversation room
                var sessionAttributes = headerAccessor.getSessionAttributes();
                if (sessionAttributes != null) {
                    sessionAttributes.put("dmConversationId", conversationId);
                }
                
                // Send confirmation to user
                Map<String, Object> response = new HashMap<>();
                response.put("type", "JOINED");
                response.put("conversationId", conversationId);
                response.put("timestamp", System.currentTimeMillis());
                
                messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/dm.joined", response);
                logger.info("User " + principal.getName() + " joined DM conversation: " + conversationId);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error handling DM join message", e);
        }
    }

    @MessageMapping("/dm.sendMessage")
    @Transactional
    public void sendMessage(@Payload DirectMessageDto directMessage, Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        try {
            // Get user from principal
            User currentUser = userService.getUserByEmail(principal.getName());
            
            // Send message using existing service
            DirectMessageDto savedMessage = directMessageService.sendMessage(
                    directMessage.getConversationId(), 
                    currentUser, 
                    directMessage.getContent()
            );
            
            // If client provided a tempId, include it in response for tracking
            if (directMessage.getTempId() != null) {
                savedMessage.setTempId(directMessage.getTempId());
            }
            
            // Create a broadcast message that shows the correct sender for all users
            DirectMessageDto broadcastMessage = new DirectMessageDto();
            broadcastMessage.setId(savedMessage.getId());
            broadcastMessage.setConversationId(savedMessage.getConversationId());
            broadcastMessage.setSenderId(savedMessage.getSenderId());
            broadcastMessage.setSenderName(savedMessage.getSenderName());
            broadcastMessage.setContent(savedMessage.getContent());
            broadcastMessage.setSentAt(savedMessage.getSentAt());
            broadcastMessage.setRead(savedMessage.isRead());
            broadcastMessage.setTempId(savedMessage.getTempId());
            
            // The currentUserSender will be determined by each client based on senderId
            // We don't set it here - let the frontend determine it
            
            // Broadcast to DM conversation topic - this will send to all subscribers including the sender
            messagingTemplate.convertAndSend(
                    "/topic/dm." + directMessage.getConversationId(), 
                    broadcastMessage
            );
            
            logger.info("DM sent via WebSocket: " + savedMessage.getContent());
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in WebSocket DM handling", e);
        }
    }
} 