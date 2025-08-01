package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.dto.DirectMessageDto;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.service.DirectMessageService;
import com.greenlink.greenlink.service.UserService;
import com.greenlink.greenlink.service.ChallengeTrackingService;
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
    
    @Autowired
    private ChallengeTrackingService challengeTrackingService;
    
   
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
         
                messagingTemplate.convertAndSend("/topic/dm." + conversationId, response);
                logger.info("DM Ping response sent to conversation: " + conversationId + " by user: " + principal.getName());
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error handling DM ping message", e);
        }
    }
    
    @MessageMapping("/dm.join")
    public void joinConversation(@Payload Map<String, Object> payload, Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        try {
            Long conversationId = payload.get("conversationId") != null ? 
                    Long.parseLong(payload.get("conversationId").toString()) : null;
                    
            if (conversationId != null) {
                var sessionAttributes = headerAccessor.getSessionAttributes();
                if (sessionAttributes != null) {
                    sessionAttributes.put("dmConversationId", conversationId);
                }
        
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

            User currentUser = userService.getUserByEmail(principal.getName());
            
            logger.info("Received WebSocket message - tempId: " + directMessage.getTempId() + ", content: " + directMessage.getContent());
            DirectMessageDto savedMessage = directMessageService.sendMessage(
                    directMessage.getConversationId(), 
                    currentUser, 
                    directMessage.getContent()
            );
            
            // Track challenge progress for sending first message
            challengeTrackingService.trackUserAction(currentUser.getId(), "MESSAGE_SENT", null);
        
            if (directMessage.getTempId() != null) {
                savedMessage.setTempId(directMessage.getTempId());
            }
            if (savedMessage.getId() != null) {
                Map<String, Object> messageMap = new HashMap<>();
                messageMap.put("id", savedMessage.getId());
                messageMap.put("messageId", savedMessage.getId());
                messageMap.put("conversationId", savedMessage.getConversationId());
                messageMap.put("senderId", savedMessage.getSenderId());
                messageMap.put("senderName", savedMessage.getSenderName());
                messageMap.put("content", savedMessage.getContent());
                messageMap.put("sentAt", savedMessage.getSentAt());
                messageMap.put("read", savedMessage.isRead());
                messageMap.put("currentUserSender", savedMessage.isCurrentUserSender());
                if (savedMessage.getTempId() != null) {
                    messageMap.put("tempId", savedMessage.getTempId());
                }
                logger.info("Broadcasting message - id: " + savedMessage.getId() + ", tempId: " + savedMessage.getTempId());
                messagingTemplate.convertAndSend(
                        "/topic/dm." + directMessage.getConversationId(), 
                        messageMap
                );
            } else {
                logger.warning("Saved message has null ID, cannot broadcast properly");
            }
            
            logger.info("DM sent via WebSocket: " + savedMessage.getContent());
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in WebSocket DM handling", e);
        }
    }
} 