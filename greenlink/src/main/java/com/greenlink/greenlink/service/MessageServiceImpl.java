package com.greenlink.greenlink.service;

import com.greenlink.greenlink.dto.ConversationDto;
import com.greenlink.greenlink.dto.MessageDto;
import com.greenlink.greenlink.model.Conversation;
import com.greenlink.greenlink.model.Message;
import com.greenlink.greenlink.model.Product;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.repository.ConversationRepository;
import com.greenlink.greenlink.repository.MessageRepository;
import com.greenlink.greenlink.repository.ProductRepository;
import com.greenlink.greenlink.service.ChallengeService;
import com.greenlink.greenlink.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements MessageService {

    private static final Logger logger = Logger.getLogger(MessageServiceImpl.class.getName());

    @Autowired
    private ConversationRepository conversationRepository;
    
    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ProductService productService;

    @Autowired
    private ChallengeService challengeService;

    @Override
    public List<ConversationDto> getUserConversations(User user) {
        try {
            List<Conversation> conversations = conversationRepository.findByUser(user);
            
            return conversations.stream().map(conversation -> {
                boolean isCurrentUserSeller = conversation.getSeller().getId().equals(user.getId());
                int unreadCount = (int) messageRepository.countUnreadMessagesInConversation(conversation, user);
                return ConversationDto.fromEntity(conversation, isCurrentUserSeller, unreadCount);
            }).collect(Collectors.toList());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting user conversations", e);
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional
    public Conversation getOrCreateConversation(Long productId, User currentUser) {
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new EntityNotFoundException("Product not found"));
            
            // Check if the user is not trying to message themselves
            if (product.getSeller().getId().equals(currentUser.getId())) {
                throw new IllegalStateException("You cannot start a conversation with yourself");
            }
            
            // Check if conversation already exists
            return conversationRepository.findByProductAndBuyer(product, currentUser)
                    .orElseGet(() -> {
                        // Create new conversation
                        Conversation conversation = new Conversation();
                        conversation.setProduct(product);
                        conversation.setSeller(product.getSeller());
                        conversation.setBuyer(currentUser);
                        conversation.setSellerRead(false);
                        conversation.setBuyerRead(true); // Buyer is creating it, so they've read it
                        return conversationRepository.save(conversation);
                    });
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating conversation", e);
            throw e;
        }
    }

    @Override
    public Conversation getConversationById(Long conversationId) {
        try {
            return conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new EntityNotFoundException("Conversation not found"));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting conversation by ID", e);
            throw e;
        }
    }

    @Override
    public List<MessageDto> getConversationMessages(Long conversationId, User currentUser) {
        try {
            Conversation conversation = getConversationById(conversationId);
            
            // Security check - only participants can view messages
            if (!conversation.getSeller().getId().equals(currentUser.getId()) && 
                !conversation.getBuyer().getId().equals(currentUser.getId())) {
                throw new IllegalStateException("You are not authorized to view this conversation");
            }
            
            List<Message> messages = messageRepository.findByConversationOrderBySentAtAsc(conversation);
            
            return messages.stream()
                    .map(message -> MessageDto.fromEntity(message, currentUser.getId()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting conversation messages", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public MessageDto sendMessage(Long conversationId, User sender, String content) {
        try {
            Conversation conversation = getConversationById(conversationId);
            
            // Security check - only participants can send messages
            if (!conversation.getSeller().getId().equals(sender.getId()) && 
                !conversation.getBuyer().getId().equals(sender.getId())) {
                throw new IllegalStateException("You are not authorized to send messages in this conversation");
            }
            
            Message message = new Message();
            message.setConversation(conversation);
            message.setSender(sender);
            message.setContent(content);
            message.setRead(false);
            
            message = messageRepository.save(message);
            
            // Update conversation read status and last update time
            boolean isSenderSeller = conversation.getSeller().getId().equals(sender.getId());
            if (isSenderSeller) {
                conversation.setSellerRead(true);
                conversation.setBuyerRead(false);
            } else {
                conversation.setSellerRead(false);
                conversation.setBuyerRead(true);
            }
            
            conversationRepository.save(conversation);
            
            // Trigger challenge event for first message
            challengeService.updateProgressByEvent(sender.getId(), "message_sent", 1);
            
            return MessageDto.fromEntity(message, sender.getId());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error sending message", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public MessageDto makeOffer(Long conversationId, User sender, Double offerAmount) {
        try {
            Conversation conversation = getConversationById(conversationId);
            
            // Security check - only participants can make offers
            if (!conversation.getSeller().getId().equals(sender.getId()) && 
                !conversation.getBuyer().getId().equals(sender.getId())) {
                throw new IllegalStateException("You are not authorized to make offers in this conversation");
            }
            
            // Create offer message
            Message message = new Message();
            message.setConversation(conversation);
            message.setSender(sender);
            
            // Set offer-specific fields
            message.setOfferAmount(offerAmount);
            message.setOfferStatus(Message.OfferStatus.PENDING);
            
            // Create content for the offer message
            String offerText = String.format("Offered %.2f RON for this product", offerAmount);
            message.setContent(offerText);
            
            message = messageRepository.save(message);
            
            // Update conversation read status
            boolean isSenderSeller = conversation.getSeller().getId().equals(sender.getId());
            if (isSenderSeller) {
                conversation.setSellerRead(true);
                conversation.setBuyerRead(false);
            } else {
                conversation.setSellerRead(false);
                conversation.setBuyerRead(true);
            }
            
            conversationRepository.save(conversation);
            
            return MessageDto.fromEntity(message, sender.getId());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error making offer", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public MessageDto respondToOffer(Long messageId, User responder, String action, Double counterOfferAmount) {
        try {
            Message offerMessage = messageRepository.findById(messageId)
                    .orElseThrow(() -> new EntityNotFoundException("Offer message not found"));
            
            Conversation conversation = offerMessage.getConversation();
            
            // Security check - only the recipient of the offer can respond to it
            boolean isResponderSeller = conversation.getSeller().getId().equals(responder.getId());
            boolean isResponderBuyer = conversation.getBuyer().getId().equals(responder.getId());
            
            if (!isResponderSeller && !isResponderBuyer) {
                throw new IllegalStateException("You are not authorized to respond to this offer");
            }
            
            if (offerMessage.getSender().getId().equals(responder.getId())) {
                throw new IllegalStateException("You cannot respond to your own offer");
            }
            
            // Check if offer is still pending
            if (offerMessage.getOfferStatus() != Message.OfferStatus.PENDING) {
                throw new IllegalStateException("This offer has already been " + offerMessage.getOfferStatus().toString().toLowerCase());
            }
            
            // Update the original offer status
            switch (action.toUpperCase()) {
                case "ACCEPT":
                    offerMessage.setOfferStatus(Message.OfferStatus.ACCEPTED);
                    // Set negotiated price for both buyer and seller
                    productService.setNegotiatedPrice(
                        conversation.getProduct().getId(), 
                        conversation.getBuyer().getId(), 
                        offerMessage.getOfferAmount()
                    );
                    productService.setNegotiatedPrice(
                        conversation.getProduct().getId(), 
                        conversation.getSeller().getId(), 
                        offerMessage.getOfferAmount()
                    );
                    break;
                case "REJECT":
                    offerMessage.setOfferStatus(Message.OfferStatus.REJECTED);
                    break;
                case "COUNTER":
                    offerMessage.setOfferStatus(Message.OfferStatus.COUNTERED);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid action: " + action);
            }
            
            messageRepository.save(offerMessage);
            
            // Create response message
            Message responseMessage = new Message();
            responseMessage.setConversation(conversation);
            responseMessage.setSender(responder);
            
            String responseContent;
            
            if ("COUNTER".equalsIgnoreCase(action) && counterOfferAmount != null) {
                responseMessage.setOfferAmount(counterOfferAmount);
                responseMessage.setOfferStatus(Message.OfferStatus.PENDING);
                responseContent = String.format("Countered with %.2f RON", counterOfferAmount);
            } else {
                responseContent = String.format("Offer of %.2f RON was %s", 
                        offerMessage.getOfferAmount(),
                        action.toLowerCase());
            }
            
            responseMessage.setContent(responseContent);
            responseMessage = messageRepository.save(responseMessage);
            
            // Update conversation read status
            if (isResponderSeller) {
                conversation.setSellerRead(true);
                conversation.setBuyerRead(false);
            } else {
                conversation.setSellerRead(false);
                conversation.setBuyerRead(true);
            }
            
            conversationRepository.save(conversation);
            
            return MessageDto.fromEntity(responseMessage, responder.getId());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error responding to offer", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void markConversationAsRead(Long conversationId, User currentUser) {
        try {
            Conversation conversation = getConversationById(conversationId);
            
            // Security check - only participants can mark as read
            if (!conversation.getSeller().getId().equals(currentUser.getId()) && 
                !conversation.getBuyer().getId().equals(currentUser.getId())) {
                throw new IllegalStateException("You are not authorized to access this conversation");
            }
            
            boolean isCurrentUserSeller = conversation.getSeller().getId().equals(currentUser.getId());
            
            // Mark conversation as read for the current user
            if (isCurrentUserSeller) {
                conversation.setSellerRead(true);
            } else {
                conversation.setBuyerRead(true);
            }
            
            conversationRepository.save(conversation);
            
            // Mark all messages from the other user as read
            List<Message> unreadMessages = messageRepository.findByConversationOrderBySentAtAsc(conversation)
                    .stream()
                    .filter(m -> !m.getSender().getId().equals(currentUser.getId()) && !m.isRead())
                    .collect(Collectors.toList());
            
            for (Message message : unreadMessages) {
                message.setRead(true);
            }
            
            if (!unreadMessages.isEmpty()) {
                messageRepository.saveAll(unreadMessages);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error marking conversation as read", e);
            throw e;
        }
    }

    @Override
    public long countUnreadMessages(User user) {
        try {
            return messageRepository.countTotalUnreadMessages(user);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error counting unread messages", e);
            return 0;
        }
    }
} 