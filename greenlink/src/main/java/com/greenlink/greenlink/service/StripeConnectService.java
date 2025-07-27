package com.greenlink.greenlink.service;

import com.greenlink.greenlink.dto.StripeConnectAccountDto;
import com.greenlink.greenlink.dto.StripeProductDto;
import com.greenlink.greenlink.dto.StripeCheckoutDto;

import java.util.List;

/**
 * Service interface for Stripe Connect operations
 * Handles connected account creation, onboarding, product management, and checkout
 */
public interface StripeConnectService {
    
    /**
     * Creates a new Stripe Connect account for a user
     * 
     * @param accountDto The account information
     * @return The created account with account ID and onboarding URL
     * @throws Exception if account creation fails
     */
    StripeConnectAccountDto createConnectedAccount(StripeConnectAccountDto accountDto) throws Exception;
    
    /**
     * Retrieves the status of a connected account
     * 
     * @param accountId The Stripe account ID
     * @return The account status information
     * @throws Exception if account retrieval fails
     */
    StripeConnectAccountDto getAccountStatus(String accountId) throws Exception;
    
    /**
     * Creates an account link for onboarding
     * 
     * @param accountId The Stripe account ID
     * @param refreshUrl The URL to redirect to when the user refreshes the page
     * @param returnUrl The URL to redirect to when the user completes onboarding
     * @return The account link URL
     * @throws Exception if account link creation fails
     */
    String createAccountLink(String accountId, String refreshUrl, String returnUrl) throws Exception;
    
    /**
     * Creates a product on a connected account
     * 
     * @param productDto The product information
     * @return The created product with product ID and price ID
     * @throws Exception if product creation fails
     */
    StripeProductDto createProduct(StripeProductDto productDto) throws Exception;
    
    /**
     * Retrieves all products for a connected account
     * 
     * @param connectedAccountId The connected account ID
     * @return List of products
     * @throws Exception if product retrieval fails
     */
    List<StripeProductDto> getProducts(String connectedAccountId) throws Exception;
    
    /**
     * Retrieves a specific product
     * 
     * @param productId The product ID
     * @param connectedAccountId The connected account ID
     * @return The product information
     * @throws Exception if product retrieval fails
     */
    StripeProductDto getProduct(String productId, String connectedAccountId) throws Exception;
    
    /**
     * Creates a checkout session for a customer to purchase products
     * 
     * @param checkoutDto The checkout information
     * @return The checkout session with URL
     * @throws Exception if checkout session creation fails
     */
    StripeCheckoutDto createCheckoutSession(StripeCheckoutDto checkoutDto) throws Exception;
    
    /**
     * Retrieves a checkout session
     * 
     * @param sessionId The checkout session ID
     * @param connectedAccountId The connected account ID
     * @return The checkout session information
     * @throws Exception if session retrieval fails
     */
    StripeCheckoutDto getCheckoutSession(String sessionId, String connectedAccountId) throws Exception;
} 