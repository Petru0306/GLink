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
            
            // Broadcast to conversation topic
            messagingTemplate.convertAndSend(
                    "/topic/conversation." + chatMessage.getConversationId(), 
                    responseMessage
            );
            
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
            
            // Broadcast to conversation topic
            messagingTemplate.convertAndSend(
                    "/topic/conversation." + chatMessage.getConversationId(),
                    responseMessage
            );
            
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
            
            // Broadcast to conversation topic
            messagingTemplate.convertAndSend(
                    "/topic/conversation." + chatMessage.getConversationId(),
                    responseMessage
            );
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in WebSocket offer response handling", e);
        }
    }
} 