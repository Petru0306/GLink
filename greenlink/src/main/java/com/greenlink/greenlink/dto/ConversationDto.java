package com.greenlink.greenlink.dto;

import com.greenlink.greenlink.model.Conversation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDto {
    private Long id;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private Double productPrice;
    private Long sellerId;
    private String sellerName;
    private Long buyerId;
    private String buyerName;
    private LocalDateTime lastMessageTime;
    private String lastMessagePreview;
    private boolean hasUnreadMessages;
    private int unreadCount;
    
    public static ConversationDto fromEntity(Conversation conversation, boolean isCurrentUserSeller, int unreadCount) {
        String lastMessagePreview = conversation.getMessages().isEmpty() ? 
                "No messages yet" : 
                conversation.getMessages().get(conversation.getMessages().size() - 1).getContent();
        
        // Truncate message preview if needed
        if (lastMessagePreview.length() > 50) {
            lastMessagePreview = lastMessagePreview.substring(0, 47) + "...";
        }
        
        return ConversationDto.builder()
                .id(conversation.getId())
                .productId(conversation.getProduct().getId())
                .productName(conversation.getProduct().getName())
                .productImageUrl(conversation.getProduct().getImageUrl())
                .productPrice(conversation.getProduct().getPrice())
                .sellerId(conversation.getSeller().getId())
                .sellerName(conversation.getSeller().getFirstName() + " " + conversation.getSeller().getLastName())
                .buyerId(conversation.getBuyer().getId())
                .buyerName(conversation.getBuyer().getFirstName() + " " + conversation.getBuyer().getLastName())
                .lastMessageTime(conversation.getUpdatedAt())
                .lastMessagePreview(lastMessagePreview)
                .hasUnreadMessages(isCurrentUserSeller ? !conversation.isSellerRead() : !conversation.isBuyerRead())
                .unreadCount(unreadCount)
                .build();
    }
} 