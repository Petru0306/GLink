package com.greenlink.greenlink.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for Stripe Connect account information
 * Used for creating connected accounts and retrieving account status
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StripeConnectAccountDto {
    
    /**
     * The Stripe account ID (starts with 'acct_')
     */
    private String accountId;
    
    /**
     * The business name for the connected account
     */
    private String businessName;
    
    /**
     * The email address for the connected account
     */
    private String email;
    
    /**
     * The country code for the connected account (e.g., 'US', 'CA')
     */
    private String country;
    
    /**
     * The account status (e.g., 'pending', 'active', 'restricted')
     */
    private String status;
    
    /**
     * Whether the account has completed onboarding
     */
    private Boolean chargesEnabled;
    
    /**
     * Whether the account can receive transfers
     */
    private Boolean payoutsEnabled;
    
    /**
     * The account type (e.g., 'express', 'standard', 'custom')
     */
    private String type;
    
    /**
     * The account link URL for onboarding (if applicable)
     */
    private String accountLinkUrl;
    
    /**
     * The refresh URL for the account link
     */
    private String refreshUrl;
    
    /**
     * The return URL for the account link
     */
    private String returnUrl;
} 