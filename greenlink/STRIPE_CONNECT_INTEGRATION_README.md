# Stripe Connect Integration for GreenLink

This document provides a comprehensive guide to the Stripe Connect integration implemented in the GreenLink application. The integration allows you to onboard sellers, create products, and process payments through connected accounts.

## 🚀 Features

- **Connected Account Creation**: Create Express accounts for sellers with platform-controlled fees
- **Onboarding Management**: Handle seller onboarding through Stripe Account Links
- **Product Management**: Create and manage products on connected accounts
- **Storefront**: Public-facing store for customers to browse and purchase products
- **Payment Processing**: Direct charges with application fees for platform monetization
- **Real-time Status**: Check account status directly from Stripe API

## 📋 Prerequisites

1. **Stripe Account**: You need a Stripe account with Connect enabled
2. **API Keys**: Configure your Stripe API keys in the application
3. **Java 17+**: The application requires Java 17 or higher
4. **Spring Boot**: The integration is built on Spring Boot 3.x

## 🔧 Configuration

### 1. Environment Variables

Set the following environment variables in your production environment:

```bash
# Stripe Configuration
STRIPE_SECRET_KEY=sk_live_your_live_secret_key_here
STRIPE_PUBLISHABLE_KEY=pk_live_your_live_publishable_key_here
STRIPE_WEBHOOK_SECRET=whsec_your_webhook_secret_here
STRIPE_PLATFORM_COMMISSION_PERCENTAGE=5.0
```

For development/testing, you can use test keys:

```bash
STRIPE_SECRET_KEY=sk_test_your_test_secret_key_here
STRIPE_PUBLISHABLE_KEY=pk_test_your_test_publishable_key_here
```

### 2. Application Properties

The configuration is handled in `application.properties`:

```properties
# Stripe Configuration
stripe.secret-key=${STRIPE_SECRET_KEY:sk_test_default_key}
stripe.publishable-key=${STRIPE_PUBLISHABLE_KEY:pk_test_default_key}
stripe.webhook-secret=${STRIPE_WEBHOOK_SECRET:whsec_default_secret}
stripe.platform-commission-percentage=5.0
```

## 🏗️ Architecture

### Core Components

1. **StripeConfig**: Configuration class for Stripe API settings
2. **StripeConnectService**: Service interface for Connect operations
3. **StripeConnectServiceImpl**: Implementation of Connect operations
4. **StripeConnectController**: REST controller for handling requests
5. **DTOs**: Data transfer objects for API communication

### Key Classes

- `StripeConnectAccountDto`: Account creation and status data
- `StripeProductDto`: Product information and pricing
- `StripeCheckoutDto`: Checkout session data

## 📖 API Endpoints

### Dashboard
- `GET /stripe-connect/dashboard` - Main dashboard

### Account Management
- `GET /stripe-connect/create-account` - Account creation form
- `POST /stripe-connect/create-account` - Create connected account
- `GET /stripe-connect/account/{accountId}` - View account status
- `POST /stripe-connect/account/{accountId}/onboard` - Create onboarding link

### Product Management
- `GET /stripe-connect/account/{accountId}/products/create` - Product creation form
- `POST /stripe-connect/account/{accountId}/products` - Create product
- `GET /stripe-connect/account/{accountId}/products` - List products

### Storefront
- `GET /stripe-connect/store/{accountId}` - Public storefront
- `POST /stripe-connect/checkout` - Create checkout session

### Payment Handling
- `GET /stripe-connect/success` - Payment success page
- `GET /stripe-connect/cancel` - Payment cancellation page

## 🔄 Workflow

### 1. Creating Connected Accounts

```java
// Create account with controller configuration
AccountCreateParams params = AccountCreateParams.builder()
    .setEmail("seller@example.com")
    .setCountry("US")
    .setType(AccountCreateParams.Type.EXPRESS)
    .setController(AccountCreateParams.Controller.builder()
        .setFees(AccountCreateParams.Controller.Fees.builder()
            .setPayer(AccountCreateParams.Controller.Fees.Payer.ACCOUNT)
            .build())
        .setLosses(AccountCreateParams.Controller.Losses.builder()
            .setPayments(AccountCreateParams.Controller.Losses.Payments.STRIPE)
            .build())
        .setStripeDashboard(AccountCreateParams.Controller.StripeDashboard.builder()
            .setType(AccountCreateParams.Controller.StripeDashboard.Type.FULL)
            .build())
        .build())
    .build();

Account account = Account.create(params);
```

### 2. Onboarding Sellers

```java
// Create account link for onboarding
AccountLinkCreateParams params = AccountLinkCreateParams.builder()
    .setAccount(accountId)
    .setRefreshUrl("https://yourapp.com/refresh")
    .setReturnUrl("https://yourapp.com/return")
    .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
    .build();

AccountLink accountLink = AccountLink.create(params);
```

### 3. Creating Products

```java
// Create product on connected account
RequestOptions requestOptions = RequestOptions.builder()
    .setStripeAccount(connectedAccountId)
    .build();

ProductCreateParams productParams = ProductCreateParams.builder()
    .setName("Product Name")
    .setDescription("Product Description")
    .setActive(true)
    .build();

Product product = Product.create(productParams, requestOptions);

// Create price for the product
PriceCreateParams priceParams = PriceCreateParams.builder()
    .setProduct(product.getId())
    .setUnitAmount(1000L) // $10.00 in cents
    .setCurrency("usd")
    .build();

Price price = Price.create(priceParams, requestOptions);
```

### 4. Processing Payments

```java
// Create checkout session with application fee
SessionCreateParams params = SessionCreateParams.builder()
    .setMode(SessionCreateParams.Mode.PAYMENT)
    .setSuccessUrl("https://yourapp.com/success")
    .setCancelUrl("https://yourapp.com/cancel")
    .addLineItem(SessionCreateParams.LineItem.builder()
        .setPrice(priceId)
        .setQuantity(1L)
        .build())
    .setPaymentIntentData(SessionCreateParams.PaymentIntentData.builder()
        .setApplicationFeeAmount(50L) // $0.50 application fee
        .build())
    .build();

RequestOptions requestOptions = RequestOptions.builder()
    .setStripeAccount(connectedAccountId)
    .build();

Session session = Session.create(params, requestOptions);
```

## 🎨 UI Components

### Dashboard
- Overview of all connected accounts
- Quick access to account creation and management
- Status indicators for accounts and products

### Account Management
- Form for creating new connected accounts
- Real-time status display with onboarding links
- Account details and verification status

### Product Management
- Product creation form with validation
- Product listing with status indicators
- Copy-to-clipboard functionality for IDs

### Storefront
- Clean, responsive product grid
- Shopping cart functionality
- Checkout integration with Stripe

## 🔒 Security Considerations

1. **API Key Management**: Never commit API keys to version control
2. **Webhook Verification**: Always verify webhook signatures
3. **Input Validation**: Validate all user inputs
4. **Error Handling**: Don't expose sensitive information in error messages
5. **HTTPS**: Always use HTTPS in production

## 🧪 Testing

### Test Cards

Use these test card numbers for testing:

- **Success**: `4242424242424242`
- **Decline**: `4000000000000002`
- **Requires Authentication**: `4000002500003155`

### Test Scenarios

1. Create a connected account
2. Complete onboarding process
3. Create products
4. Process test payments
5. Verify application fees

## 🚨 Error Handling

The integration includes comprehensive error handling:

- **Validation Errors**: Form validation with user-friendly messages
- **API Errors**: Stripe API error handling with detailed logging
- **Network Errors**: Graceful handling of network issues
- **User Feedback**: Clear error messages and recovery options

## 📊 Monitoring

### Key Metrics to Monitor

1. **Account Creation Success Rate**
2. **Onboarding Completion Rate**
3. **Payment Success Rate**
4. **Application Fee Collection**
5. **Error Rates by Endpoint**

### Logging

The application logs all Stripe operations with appropriate levels:

```java
// Example logging in service
logger.info("Created connected account: {}", accountId);
logger.error("Failed to create product: {}", e.getMessage(), e);
```

## 🔄 Webhooks (Recommended)

For production use, implement webhook handling:

```java
@PostMapping("/webhook")
public ResponseEntity<String> handleWebhook(@RequestBody String payload, 
                                          @RequestHeader("Stripe-Signature") String sigHeader) {
    try {
        Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        
        switch (event.getType()) {
            case "account.updated":
                handleAccountUpdated(event);
                break;
            case "payment_intent.succeeded":
                handlePaymentSucceeded(event);
                break;
            // Handle other events
        }
        
        return ResponseEntity.ok("Webhook processed");
    } catch (Exception e) {
        return ResponseEntity.badRequest().body("Webhook error: " + e.getMessage());
    }
}
```

## 🚀 Deployment

### Production Checklist

- [ ] Set up environment variables with live Stripe keys
- [ ] Configure webhook endpoints in Stripe dashboard
- [ ] Set up monitoring and alerting
- [ ] Test all flows with live accounts
- [ ] Implement proper error tracking
- [ ] Set up backup and recovery procedures

### Environment-Specific Configuration

```properties
# Development
stripe.platform-commission-percentage=5.0
logging.level.com.stripe=DEBUG

# Production
stripe.platform-commission-percentage=5.0
logging.level.com.stripe=WARN
```

## 📚 Additional Resources

- [Stripe Connect Documentation](https://stripe.com/docs/connect)
- [Stripe Java SDK Documentation](https://stripe.com/docs/api/java)
- [Stripe Connect Best Practices](https://stripe.com/docs/connect/best-practices)
- [Stripe Connect API Reference](https://stripe.com/docs/api/connect)

## 🤝 Support

For issues with this integration:

1. Check the Stripe documentation
2. Review the application logs
3. Test with Stripe's test mode
4. Contact the development team

## 📝 Changelog

### Version 1.0.0
- Initial implementation of Stripe Connect integration
- Account creation and onboarding
- Product management
- Payment processing with application fees
- Complete UI implementation

---

**Note**: This integration uses the latest Stripe API version (2025-06-30.basil) and follows Stripe's best practices for Connect implementations. 