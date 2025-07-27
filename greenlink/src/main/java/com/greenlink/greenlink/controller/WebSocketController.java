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
                
               
                messagingTemplate.convertAndSend("/topic/conversation." + conversationId, response);
                logger.info("Ping response sent to conversation: " + conversationId + " by user: " + principal.getName());
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error handling ping message", e);
        }
    }
    
    
    @MessageMapping("/chat.join")
    public void joinConversation(@Payload Map<String, Object> payload, Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        try {
            Long conversationId = Long.parseLong(payload.get("conversationId").toString());
            
            
            User currentUser = userService.getUserByEmail(principal.getName());
            messageService.markConversationAsRead(conversationId, currentUser);
            
            
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
            
            User currentUser = userService.getUserByEmail(principal.getName());
            
            
            MessageDto savedMessage = messageService.sendMessage(
                    chatMessage.getConversationId(), 
                    currentUser, 
                    chatMessage.getContent()
            );
            
            // Track challenge progress for sending first message
            challengeTrackingService.trackUserAction(currentUser.getId(), "MESSAGE_SENT", null);
            
            ChatMessageDto responseMessage = ChatMessageDto.fromMessageDto(savedMessage);
            
            
            if (chatMessage.getTempId() != null) {
                responseMessage.setTempId(chatMessage.getTempId());
            }
            
            
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
            
            User currentUser = userService.getUserByEmail(principal.getName());
            
            
            MessageDto savedMessage = messageService.makeOffer(
                    chatMessage.getConversationId(),
                    currentUser,
                    chatMessage.getOfferAmount()
            );
            
            
            challengeTrackingService.trackUserAction(currentUser.getId(), "MARKETPLACE_OFFER_MADE", null);
            
            
            try {
                    String productName = "Product"; 
                pointsService.awardOfferMadePoints(currentUser.getId(), 1L, productName, chatMessage.getOfferAmount(), 5);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error awarding points for offer made: " + e.getMessage());
            }
            
            
            ChatMessageDto responseMessage = ChatMessageDto.fromMessageDto(savedMessage);
            responseMessage.setEventType("OFFER_CREATED");
            
            
            if (chatMessage.getTempId() != null) {
                responseMessage.setTempId(chatMessage.getTempId());
            }
            
            
            messagingTemplate.convertAndSend(
                    "/topic/conversation." + chatMessage.getConversationId(),
                    responseMessage
            );
            
            
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
            
            User currentUser = userService.getUserByEmail(principal.getName());
            
            
            MessageDto savedMessage = messageService.respondToOffer(
                    chatMessage.getMessageId(),
                    currentUser,
                    chatMessage.getOfferStatus(),
                    chatMessage.getOfferAmount()
            );
            
            
            MessageDto originalMessage = messageService.getMessageById(chatMessage.getMessageId(), currentUser);
            
            
            ChatMessageDto statusUpdate = new ChatMessageDto();
            statusUpdate.setMessageId(chatMessage.getMessageId());
            statusUpdate.setConversationId(chatMessage.getConversationId());
            statusUpdate.setOfferStatus(chatMessage.getOfferStatus());
            statusUpdate.setOfferAmount(originalMessage.getOfferAmount());
            statusUpdate.setOffer(true);
            statusUpdate.setEventType("OFFER_UPDATED");
            
            statusUpdate.setContent(null);
            
            
            messagingTemplate.convertAndSend(
                    "/topic/conversation." + chatMessage.getConversationId(),
                    statusUpdate
            );
            
            
            OfferEvent.EventType eventType = determineEventType(chatMessage.getOfferStatus());
            messagingTemplate.convertAndSend(
                    "/topic/offer." + chatMessage.getMessageId(),
                    createOfferEvent(eventType, originalMessage, currentUser, chatMessage.getOfferStatus())
            );
            
            
            if (chatMessage.getTempId() != null && savedMessage != null) {
                
                ChatMessageDto responseMessage = ChatMessageDto.fromMessageDto(savedMessage);
                responseMessage.setTempId(chatMessage.getTempId());
                responseMessage.setEventType("COUNTER_OFFER_CREATED");
                
                
                messagingTemplate.convertAndSend(
                        "/topic/conversation." + chatMessage.getConversationId(),
                        responseMessage
                );
                

                messagingTemplate.convertAndSend(
                        "/topic/offer." + savedMessage.getId(),
                        createOfferEvent(OfferEvent.EventType.COUNTER_OFFER_CREATED, savedMessage, currentUser, null)
                );
            }
            
            
            if ("ACCEPT".equalsIgnoreCase(chatMessage.getOfferStatus()) && chatMessage.getOfferAmount() != null) {
                ChatMessageDto priceUpdate = new ChatMessageDto();
                priceUpdate.setConversationId(chatMessage.getConversationId());
                priceUpdate.setOfferAmount(chatMessage.getOfferAmount());
                priceUpdate.setContent("PRICE_UPDATE"); 
                priceUpdate.setOfferStatus("PRICE_UPDATED"); 
                priceUpdate.setEventType("PRICE_UPDATED");
                
                
                messagingTemplate.convertAndSend(
                        "/topic/conversation." + chatMessage.getConversationId(),
                        priceUpdate
                );
                
                
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
    
        
    private OfferEvent.EventType determineEventType(String action) {
        if (action == null) {
            return OfferEvent.EventType.OFFER_UPDATED;
        }
        
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