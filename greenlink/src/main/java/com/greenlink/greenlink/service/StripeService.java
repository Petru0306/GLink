package com.greenlink.greenlink.service;

import com.greenlink.greenlink.model.Product;
import com.greenlink.greenlink.model.User;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.model.Customer;
import com.stripe.model.checkout.Session;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StripeService {
    
    @Value("${stripe.platform-commission-percentage}")
    private double platformCommissionPercentage;
    
    @Value("${stripe.webhook-secret}")
    private String webhookSecret;
    
    /**
     * Create a Stripe Connect Express account for a seller
     */
    public String createConnectAccount(User seller) throws StripeException {
        AccountCreateParams params = AccountCreateParams.builder()
                .setType(AccountCreateParams.Type.EXPRESS)
                .setCountry("RO") // Romania
                .setEmail(seller.getEmail())
                .setCapabilities(
                        AccountCreateParams.Capabilities.builder()
                                .setCardPayments(AccountCreateParams.Capabilities.CardPayments.builder().setRequested(true).build())
                                .setTransfers(AccountCreateParams.Capabilities.Transfers.builder().setRequested(true).build())
                                .build()
                )
                .setBusinessType(AccountCreateParams.BusinessType.INDIVIDUAL)
                .setBusinessProfile(
                        AccountCreateParams.BusinessProfile.builder()
                                .setMcc("5734") // Computer Software Stores
                                .setUrl("https://greenlink.com")
                                .build()
                )
                .build();
        
        Account account = Account.create(params);
        
        // Update user with Stripe account ID
        seller.setStripeAccountId(account.getId());
        
        return account.getId();
    }
    
    /**
     * Create an account link for onboarding
     */
    public String createAccountLink(String accountId, String returnUrl) throws StripeException {
        AccountLinkCreateParams params = AccountLinkCreateParams.builder()
                .setAccount(accountId)
                .setRefreshUrl(returnUrl + "?refresh=true")
                .setReturnUrl(returnUrl)
                .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                .build();
        
        AccountLink accountLink = AccountLink.create(params);
        return accountLink.getUrl();
    }
    
    /**
     * Create a Stripe customer for a buyer
     */
    public String createCustomer(User buyer) throws StripeException {
        CustomerCreateParams params = CustomerCreateParams.builder()
                .setEmail(buyer.getEmail())
                .setName(buyer.getFirstName() + " " + buyer.getLastName())
                .build();
        
        Customer customer = Customer.create(params);
        
        // Update user with Stripe customer ID
        buyer.setStripeCustomerId(customer.getId());
        
        return customer.getId();
    }
    
    /**
     * Create a checkout session for a product purchase
     */
    public Session createCheckoutSession(Product product, User buyer, String successUrl, String cancelUrl) throws StripeException {
        // Calculate platform commission in cents
        long productAmountCents = (long) (product.getPrice() * 100); // Convert to cents
        long commissionAmountCents = (long) (productAmountCents * platformCommissionPercentage / 100.0);
        
        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .setCustomer(buyer.getStripeCustomerId());
        
        // Build product data for line items
        SessionCreateParams.LineItem.PriceData.ProductData.Builder productDataBuilder = 
                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName(product.getName())
                        .setDescription(product.getDescription());
        
        // Only add image if it's not null or empty
        if (product.getImageUrl() != null && !product.getImageUrl().trim().isEmpty()) {
            productDataBuilder.addImage(product.getImageUrl());
        }
        
        paramsBuilder.addLineItem(
                SessionCreateParams.LineItem.builder()
                        .setPriceData(
                                SessionCreateParams.LineItem.PriceData.builder()
                                        .setCurrency("ron")
                                        .setProductData(productDataBuilder.build())
                                        .setUnitAmount(productAmountCents)
                                        .build()
                        )
                        .setQuantity(1L)
                        .build()
        )
                .putMetadata("product_id", product.getId().toString())
                .putMetadata("buyer_id", buyer.getId().toString())
                .putMetadata("seller_id", product.getSeller().getId().toString())
                .putMetadata("commission_amount", String.valueOf(commissionAmountCents));
        
        // Add Stripe Connect transfer to seller if seller has onboarded
        if (product.getSeller().getStripeAccountId() != null) {
            paramsBuilder
                .setPaymentIntentData(
                    SessionCreateParams.PaymentIntentData.builder()
                        .setApplicationFeeAmount(commissionAmountCents)
                        .setTransferData(
                            SessionCreateParams.PaymentIntentData.TransferData.builder()
                                .setDestination(product.getSeller().getStripeAccountId())
                                .build()
                        )
                        .build()
                );
        }
        
        return Session.create(paramsBuilder.build());
    }
    
    /**
     * Verify webhook signature
     */
    public boolean verifyWebhookSignature(String payload, String signature) {
        try {
            com.stripe.net.Webhook.constructEvent(payload, signature, webhookSecret);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get account status
     */
    public Map<String, Object> getAccountStatus(String accountId) throws StripeException {
        Account account = Account.retrieve(accountId);
        Map<String, Object> status = new HashMap<>();
        status.put("id", account.getId());
        status.put("chargesEnabled", account.getChargesEnabled());
        status.put("payoutsEnabled", account.getPayoutsEnabled());
        status.put("detailsSubmitted", account.getDetailsSubmitted());
        return status;
    }
    
    /**
     * Get webhook secret
     */
    public String getWebhookSecret() {
        return webhookSecret;
    }
    
    /**
     * Retrieve a checkout session by ID
     */
    public Session retrieveSession(String sessionId) throws StripeException {
        return Session.retrieve(sessionId);
    }

    /**
     * Create a direct transfer between users (peer-to-peer)
     */
    public String createDirectTransfer(User fromUser, User toUser, long amount, String description) throws StripeException {
        // Ensure both users have Stripe accounts
        if (fromUser.getStripeAccountId() == null || toUser.getStripeAccountId() == null) {
            throw new RuntimeException("Both users must have Stripe accounts for direct transfers");
        }
        
        // Create transfer from platform to seller
        com.stripe.model.Transfer transfer = com.stripe.model.Transfer.create(
            com.stripe.param.TransferCreateParams.builder()
                .setAmount(amount)
                .setCurrency("ron")
                .setDestination(toUser.getStripeAccountId())
                .setDescription(description)
                .putMetadata("from_user_id", fromUser.getId().toString())
                .putMetadata("to_user_id", toUser.getId().toString())
                .build()
        );
        
        return transfer.getId();
    }
    
    /**
     * Create instant payout for seller
     */
    public String createInstantPayout(User seller, long amount) throws StripeException {
        if (seller.getStripeAccountId() == null) {
            throw new RuntimeException("Seller must have a Stripe account");
        }
        
        com.stripe.model.Payout payout = com.stripe.model.Payout.create(
            com.stripe.param.PayoutCreateParams.builder()
                .setAmount(amount)
                .setCurrency("ron")
                .setMethod(com.stripe.param.PayoutCreateParams.Method.INSTANT)
                .build(),
            com.stripe.net.RequestOptions.builder()
                .setStripeAccount(seller.getStripeAccountId())
                .build()
        );
        
        return payout.getId();
    }
    
    /**
     * Create a payment intent for direct charges
     */
    public String createPaymentIntent(User buyer, User seller, long amount, String description) throws StripeException {
        com.stripe.model.PaymentIntent paymentIntent = com.stripe.model.PaymentIntent.create(
            com.stripe.param.PaymentIntentCreateParams.builder()
                .setAmount(amount)
                .setCurrency("ron")
                .setCustomer(buyer.getStripeCustomerId())
                .setApplicationFeeAmount((long) (amount * platformCommissionPercentage / 100.0))
                .setTransferData(
                    com.stripe.param.PaymentIntentCreateParams.TransferData.builder()
                        .setDestination(seller.getStripeAccountId())
                        .build()
                )
                .setDescription(description)
                .putMetadata("buyer_id", buyer.getId().toString())
                .putMetadata("seller_id", seller.getId().toString())
                .build()
        );
        
        return paymentIntent.getId();
    }
    
    /**
     * Get account balance for a seller
     */
    public Map<String, Object> getAccountBalance(String accountId) throws StripeException {
        com.stripe.model.Balance balance = com.stripe.model.Balance.retrieve(
            com.stripe.net.RequestOptions.builder()
                .setStripeAccount(accountId)
                .build()
        );
        
        Map<String, Object> balanceInfo = new HashMap<>();
        balanceInfo.put("available", balance.getAvailable());
        balanceInfo.put("pending", balance.getPending());
        balanceInfo.put("instant_available", balance.getInstantAvailable());
        
        return balanceInfo;
    }
    
    /**
     * Create a refund for a payment
     */
    public String createRefund(String paymentIntentId, long amount, String reason) throws StripeException {
        com.stripe.model.Refund refund = com.stripe.model.Refund.create(
            com.stripe.param.RefundCreateParams.builder()
                .setPaymentIntent(paymentIntentId)
                .setAmount(amount)
                .setReason(com.stripe.param.RefundCreateParams.Reason.valueOf(reason.toUpperCase()))
                .build()
        );
        
        return refund.getId();
    }
} 