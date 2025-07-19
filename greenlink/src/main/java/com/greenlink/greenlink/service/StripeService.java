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
        // Calculate platform commission
        long commissionAmount = (long) (product.getPrice() * platformCommissionPercentage / 100.0 * 100); // Convert to cents
        
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .setCustomer(buyer.getStripeCustomerId())
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("ron")
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(product.getName())
                                                                .setDescription(product.getDescription())
                                                                .addImage(product.getImageUrl())
                                                                .build()
                                                )
                                                .setUnitAmount((long) (product.getPrice() * 100)) // Convert to cents
                                                .build()
                                )
                                .setQuantity(1L)
                                .build()
                )
                .putMetadata("product_id", product.getId().toString())
                .putMetadata("buyer_id", buyer.getId().toString())
                .putMetadata("seller_id", product.getSeller().getId().toString())
                .putMetadata("commission_amount", String.valueOf(commissionAmount))
                .build();
        
        return Session.create(params);
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
} 