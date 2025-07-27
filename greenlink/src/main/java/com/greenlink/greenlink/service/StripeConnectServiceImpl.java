package com.greenlink.greenlink.service;

import com.greenlink.greenlink.dto.StripeConnectAccountDto;
import com.greenlink.greenlink.dto.StripeProductDto;
import com.greenlink.greenlink.dto.StripeCheckoutDto;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.model.Product;
import com.stripe.model.Price;
import com.stripe.model.checkout.Session;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import com.stripe.param.ProductCreateParams;
import com.stripe.param.PriceCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.ProductListParams;
import com.stripe.param.PriceListParams;
import com.stripe.param.checkout.SessionRetrieveParams;
import com.stripe.net.RequestOptions;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Implementation of Stripe Connect service
 * Provides comprehensive Stripe Connect integration with detailed error handling
 */
@Service
public class StripeConnectServiceImpl implements StripeConnectService {
    
    @Override
    public StripeConnectAccountDto createConnectedAccount(StripeConnectAccountDto accountDto) throws Exception {
        try {
            // Validate required fields
            if (accountDto.getEmail() == null || accountDto.getEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("Email is required for account creation");
            }
            
            if (accountDto.getCountry() == null || accountDto.getCountry().trim().isEmpty()) {
                throw new IllegalArgumentException("Country is required for account creation");
            }
            
            // Create account parameters for Express account
            // This creates an Express account that the platform can manage
            AccountCreateParams params = AccountCreateParams.builder()
                .setEmail(accountDto.getEmail())
                .setCountry(accountDto.getCountry())
                .setType(AccountCreateParams.Type.EXPRESS)
                .build();
            
            // Create the connected account
            Account account = Account.create(params);
            
            // Build response DTO with account information
            return StripeConnectAccountDto.builder()
                .accountId(account.getId())
                .email(account.getEmail())
                .country(account.getCountry())
                .status("pending")
                .chargesEnabled(account.getChargesEnabled())
                .payoutsEnabled(account.getPayoutsEnabled())
                .type(account.getType())
                .build();
                
        } catch (StripeException e) {
            throw new Exception("Failed to create connected account: " + e.getMessage(), e);
        }
    }
    
    @Override
    public StripeConnectAccountDto getAccountStatus(String accountId) throws Exception {
        try {
            // Validate account ID
            if (accountId == null || accountId.trim().isEmpty()) {
                throw new IllegalArgumentException("Account ID is required");
            }
            
            // Retrieve the account directly from Stripe API
            // This ensures we always get the most up-to-date status
            Account account = Account.retrieve(accountId);
            
            // Build response DTO with current account status
            return StripeConnectAccountDto.builder()
                .accountId(account.getId())
                .email(account.getEmail())
                .country(account.getCountry())
                .status("active")
                .chargesEnabled(account.getChargesEnabled())
                .payoutsEnabled(account.getPayoutsEnabled())
                .type(account.getType())
                .build();
                
        } catch (StripeException e) {
            throw new Exception("Failed to retrieve account status: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String createAccountLink(String accountId, String refreshUrl, String returnUrl) throws Exception {
        try {
            // Validate required parameters
            if (accountId == null || accountId.trim().isEmpty()) {
                throw new IllegalArgumentException("Account ID is required");
            }
            
            if (refreshUrl == null || refreshUrl.trim().isEmpty()) {
                throw new IllegalArgumentException("Refresh URL is required");
            }
            
            if (returnUrl == null || returnUrl.trim().isEmpty()) {
                throw new IllegalArgumentException("Return URL is required");
            }
            
            // Create account link parameters for onboarding
            AccountLinkCreateParams params = AccountLinkCreateParams.builder()
                .setAccount(accountId)
                .setRefreshUrl(refreshUrl)
                .setReturnUrl(returnUrl)
                .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                .build();
            
            // Create the account link
            AccountLink accountLink = AccountLink.create(params);
            
            return accountLink.getUrl();
            
        } catch (StripeException e) {
            throw new Exception("Failed to create account link: " + e.getMessage(), e);
        }
    }
    
    @Override
    public StripeProductDto createProduct(StripeProductDto productDto) throws Exception {
        try {
            // Validate required fields
            if (productDto.getName() == null || productDto.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Product name is required");
            }
            
            if (productDto.getPriceInCents() == null || productDto.getPriceInCents() <= 0) {
                throw new IllegalArgumentException("Valid price is required");
            }
            
            if (productDto.getConnectedAccountId() == null || productDto.getConnectedAccountId().trim().isEmpty()) {
                throw new IllegalArgumentException("Connected account ID is required");
            }
            
            // Set up request options for the connected account
            RequestOptions requestOptions = RequestOptions.builder()
                .setStripeAccount(productDto.getConnectedAccountId())
                .build();
            
            // Create product parameters
            ProductCreateParams productParams = ProductCreateParams.builder()
                .setName(productDto.getName())
                .setDescription(productDto.getDescription())
                .setActive(productDto.getActive() != null ? productDto.getActive() : true)
                .build();
            
            // Create the product on the connected account
            Product product = Product.create(productParams, requestOptions);
            
            // Create price parameters
            PriceCreateParams priceParams = PriceCreateParams.builder()
                .setProduct(product.getId())
                .setUnitAmount(productDto.getPriceInCents())
                .setCurrency(productDto.getCurrency() != null ? productDto.getCurrency() : "usd")
                .build();
            
            // Create the price on the connected account
            Price price = Price.create(priceParams, requestOptions);
            
            // Build response DTO with product and price information
            return StripeProductDto.builder()
                .productId(product.getId())
                .priceId(price.getId())
                .name(product.getName())
                .description(product.getDescription())
                .priceInCents(price.getUnitAmount())
                .currency(price.getCurrency())
                .connectedAccountId(productDto.getConnectedAccountId())
                .active(product.getActive())
                .build();
                
        } catch (StripeException e) {
            throw new Exception("Failed to create product: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<StripeProductDto> getProducts(String connectedAccountId) throws Exception {
        try {
            // Validate connected account ID
            if (connectedAccountId == null || connectedAccountId.trim().isEmpty()) {
                throw new IllegalArgumentException("Connected account ID is required");
            }
            
            // Set up request options for the connected account
            RequestOptions requestOptions = RequestOptions.builder()
                .setStripeAccount(connectedAccountId)
                .build();
            
            // List products for the connected account
            ProductListParams productParams = ProductListParams.builder()
                .setActive(true)
                .setLimit(100L)
                .build();
            
            List<Product> products = Product.list(productParams, requestOptions).getData();
            
            // Convert products to DTOs
            List<StripeProductDto> productDtos = new ArrayList<>();
            
            for (Product product : products) {
                // Get the default price for each product
                PriceListParams priceParams = PriceListParams.builder()
                    .setProduct(product.getId())
                    .setActive(true)
                    .setLimit(1L)
                    .build();
                
                List<Price> prices = Price.list(priceParams, requestOptions).getData();
                
                if (!prices.isEmpty()) {
                    Price price = prices.get(0);
                    
                    StripeProductDto productDto = StripeProductDto.builder()
                        .productId(product.getId())
                        .priceId(price.getId())
                        .name(product.getName())
                        .description(product.getDescription())
                        .priceInCents(price.getUnitAmount())
                        .currency(price.getCurrency())
                        .connectedAccountId(connectedAccountId)
                        .active(product.getActive())
                        .build();
                    
                    productDtos.add(productDto);
                }
            }
            
            return productDtos;
            
        } catch (StripeException e) {
            throw new Exception("Failed to retrieve products: " + e.getMessage(), e);
        }
    }
    
    @Override
    public StripeProductDto getProduct(String productId, String connectedAccountId) throws Exception {
        try {
            // Validate parameters
            if (productId == null || productId.trim().isEmpty()) {
                throw new IllegalArgumentException("Product ID is required");
            }
            
            if (connectedAccountId == null || connectedAccountId.trim().isEmpty()) {
                throw new IllegalArgumentException("Connected account ID is required");
            }
            
            // Set up request options for the connected account
            RequestOptions requestOptions = RequestOptions.builder()
                .setStripeAccount(connectedAccountId)
                .build();
            
            // Retrieve the product
            Product product = Product.retrieve(productId, requestOptions);
            
            // Get the default price for the product
            PriceListParams priceParams = PriceListParams.builder()
                .setProduct(product.getId())
                .setActive(true)
                .setLimit(1L)
                .build();
            
            List<Price> prices = Price.list(priceParams, requestOptions).getData();
            
            if (prices.isEmpty()) {
                throw new Exception("No active price found for product: " + productId);
            }
            
            Price price = prices.get(0);
            
            // Build response DTO
            return StripeProductDto.builder()
                .productId(product.getId())
                .priceId(price.getId())
                .name(product.getName())
                .description(product.getDescription())
                .priceInCents(price.getUnitAmount())
                .currency(price.getCurrency())
                .connectedAccountId(connectedAccountId)
                .active(product.getActive())
                .build();
                
        } catch (StripeException e) {
            throw new Exception("Failed to retrieve product: " + e.getMessage(), e);
        }
    }
    
    @Override
    public StripeCheckoutDto createCheckoutSession(StripeCheckoutDto checkoutDto) throws Exception {
        try {
            // Validate required fields
            if (checkoutDto.getConnectedAccountId() == null || checkoutDto.getConnectedAccountId().trim().isEmpty()) {
                throw new IllegalArgumentException("Connected account ID is required");
            }
            
            if (checkoutDto.getLineItems() == null || checkoutDto.getLineItems().isEmpty()) {
                throw new IllegalArgumentException("At least one line item is required");
            }
            
            if (checkoutDto.getSuccessUrl() == null || checkoutDto.getSuccessUrl().trim().isEmpty()) {
                throw new IllegalArgumentException("Success URL is required");
            }
            
            if (checkoutDto.getCancelUrl() == null || checkoutDto.getCancelUrl().trim().isEmpty()) {
                throw new IllegalArgumentException("Cancel URL is required");
            }
            
            // Set up request options for the connected account
            RequestOptions requestOptions = RequestOptions.builder()
                .setStripeAccount(checkoutDto.getConnectedAccountId())
                .build();
            
            // Build line items for the checkout session
            List<SessionCreateParams.LineItem> lineItems = checkoutDto.getLineItems().stream()
                .map(item -> SessionCreateParams.LineItem.builder()
                    .setPrice(item.getPriceId())
                    .setQuantity((long) item.getQuantity())
                    .build())
                .collect(Collectors.toList());
            
            // Build checkout session parameters
            SessionCreateParams.Builder sessionParamsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(checkoutDto.getSuccessUrl())
                .setCancelUrl(checkoutDto.getCancelUrl())
                .addAllLineItem(lineItems);
            
            // Add application fee if specified
            if (checkoutDto.getApplicationFeeAmount() != null && checkoutDto.getApplicationFeeAmount() > 0) {
                sessionParamsBuilder.setPaymentIntentData(
                    SessionCreateParams.PaymentIntentData.builder()
                        .setApplicationFeeAmount(checkoutDto.getApplicationFeeAmount())
                        .build()
                );
            }
            
            // Add customer email if specified
            if (checkoutDto.getCustomerEmail() != null && !checkoutDto.getCustomerEmail().trim().isEmpty()) {
                sessionParamsBuilder.setCustomerEmail(checkoutDto.getCustomerEmail());
            }
            
            SessionCreateParams sessionParams = sessionParamsBuilder.build();
            
            // Create the checkout session
            Session session = Session.create(sessionParams, requestOptions);
            
            // Build response DTO
            return StripeCheckoutDto.builder()
                .sessionId(session.getId())
                .checkoutUrl(session.getUrl())
                .connectedAccountId(checkoutDto.getConnectedAccountId())
                .lineItems(checkoutDto.getLineItems())
                .applicationFeeAmount(checkoutDto.getApplicationFeeAmount())
                .successUrl(checkoutDto.getSuccessUrl())
                .cancelUrl(checkoutDto.getCancelUrl())
                .mode(session.getMode())
                .currency(session.getCurrency())
                .customerEmail(session.getCustomerEmail())
                .build();
                
        } catch (StripeException e) {
            throw new Exception("Failed to create checkout session: " + e.getMessage(), e);
        }
    }
    
    @Override
    public StripeCheckoutDto getCheckoutSession(String sessionId, String connectedAccountId) throws Exception {
        try {
            // Validate parameters
            if (sessionId == null || sessionId.trim().isEmpty()) {
                throw new IllegalArgumentException("Session ID is required");
            }
            
            if (connectedAccountId == null || connectedAccountId.trim().isEmpty()) {
                throw new IllegalArgumentException("Connected account ID is required");
            }
            
            // Set up request options for the connected account
            RequestOptions requestOptions = RequestOptions.builder()
                .setStripeAccount(connectedAccountId)
                .build();
            
            // Retrieve the checkout session
            SessionRetrieveParams params = SessionRetrieveParams.builder().build();
            Session session = Session.retrieve(sessionId, params, requestOptions);
            
            // Convert line items to DTO format
            List<StripeCheckoutDto.CheckoutLineItem> lineItems = session.getLineItems().getData().stream()
                .map(item -> StripeCheckoutDto.CheckoutLineItem.builder()
                    .priceId(item.getPrice().getId())
                    .quantity(item.getQuantity().intValue())
                    .name(item.getDescription())
                    .build())
                .collect(Collectors.toList());
            
            // Build response DTO
            return StripeCheckoutDto.builder()
                .sessionId(session.getId())
                .checkoutUrl(session.getUrl())
                .connectedAccountId(connectedAccountId)
                .lineItems(lineItems)
                .successUrl(session.getSuccessUrl())
                .cancelUrl(session.getCancelUrl())
                .mode(session.getMode())
                .currency(session.getCurrency())
                .customerEmail(session.getCustomerEmail())
                .build();
                
        } catch (StripeException e) {
            throw new Exception("Failed to retrieve checkout session: " + e.getMessage(), e);
        }
    }
} 