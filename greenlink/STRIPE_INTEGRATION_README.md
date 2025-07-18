# Stripe Integration for GreenLink Marketplace

This document describes the implementation of a full buy system using Stripe Connect in the GreenLink Spring Boot marketplace.

## Overview

The implementation provides a complete payment system with the following features:

- **Stripe Connect Express** for seller onboarding
- **Stripe Checkout** for secure payments
- **Platform commission** handling
- **Sales history** tracking
- **Webhook processing** for payment confirmations
- **Buy buttons** on product details and conversation pages

## Architecture

### Core Components

1. **StripeService** - Handles all Stripe API interactions
2. **PaymentService** - Manages payment processing and product state updates
3. **PaymentController** - REST endpoints for payment operations
4. **SalesController** - Sales history and statistics
5. **Database Models** - Updated User and Product models with Stripe fields

### Database Schema Changes

#### User Table
- `stripe_account_id` - Stripe Connect account ID for sellers
- `stripe_customer_id` - Stripe customer ID for buyers

#### Product Table
- `sold` - Boolean flag indicating if product is sold
- `buyer_id` - Foreign key to User table (buyer)
- `sold_at` - Timestamp when product was sold

## Configuration

### Application Properties

Add the following to `application.properties`:

```properties
# Stripe Configuration
stripe.secret-key=sk_test_your_stripe_secret_key_here
stripe.publishable-key=pk_test_your_stripe_publishable_key_here
stripe.webhook-secret=whsec_your_webhook_secret_here
stripe.platform-commission-percentage=5.0
```

### Dependencies

Add to `pom.xml`:

```xml
<dependency>
    <groupId>com.stripe</groupId>
    <artifactId>stripe-java</artifactId>
    <version>24.8.0</version>
</dependency>
```

## API Endpoints

### Payment Endpoints

- `POST /payment/create-checkout/{productId}` - Create Stripe Checkout session
- `POST /payment/webhook` - Handle Stripe webhooks
- `POST /payment/onboard-seller` - Onboard seller to Stripe Connect
- `GET /payment/account-status` - Get seller account status
- `GET /payment/success` - Payment success page
- `GET /payment/cancel` - Payment cancel page

### Sales Endpoints

- `GET /dashboard/sales` - Sales history page

## User Flow

### Seller Onboarding

1. Seller clicks "Start Selling" or tries to list a product
2. System checks if seller has Stripe account
3. If not, creates Stripe Connect Express account
4. Redirects to Stripe onboarding flow
5. Seller completes onboarding and returns to platform

### Product Purchase

1. Buyer clicks "Buy Now" button on product details or conversation
2. System creates Stripe Checkout session with:
   - Product details
   - Platform commission
   - Transfer to seller's account
3. Buyer completes payment on Stripe
4. Webhook processes successful payment
5. Product is marked as sold
6. Buyer and seller are notified

## Implementation Details

### Stripe Connect Express

```java
// Create Connect account
AccountCreateParams params = AccountCreateParams.builder()
    .setType(AccountCreateParams.Type.EXPRESS)
    .setCountry("RO")
    .setEmail(seller.getEmail())
    .setCapabilities(
        AccountCreateParams.Capabilities.builder()
            .setCardPayments(AccountCreateParams.Capabilities.CardPayments.builder().setRequested(true).build())
            .setTransfers(AccountCreateParams.Capabilities.Transfers.builder().setRequested(true).build())
            .build()
    )
    .build();
```

### Checkout Session Creation

```java
SessionCreateParams params = SessionCreateParams.builder()
    .setMode(SessionCreateParams.Mode.PAYMENT)
    .setSuccessUrl(successUrl)
    .setCancelUrl(cancelUrl)
    .setCustomer(buyer.getStripeCustomerId())
    .addLineItem(/* product line item */)
    .putMetadata("transfer_destination", seller.getStripeAccountId())
    .putMetadata("application_fee_amount", String.valueOf(commissionAmount))
    .build();
```

### Webhook Processing

```java
@PostMapping("/webhook")
public ResponseEntity<String> handleWebhook(HttpServletRequest request) {
    // Verify webhook signature
    // Parse event
    // Handle checkout.session.completed
    // Update product state
}
```

## Security Considerations

1. **Webhook Signature Verification** - All webhooks are verified using Stripe's signature
2. **CSRF Protection** - All payment endpoints are protected
3. **Authentication** - Payment operations require user authentication
4. **Input Validation** - All inputs are validated and sanitized
5. **Error Handling** - Comprehensive error handling and logging

## Testing

### Unit Tests

Run the test suite:

```bash
mvn test -Dtest=PaymentServiceTest
```

### Integration Tests

1. Set up test Stripe keys
2. Test seller onboarding flow
3. Test payment processing
4. Test webhook handling

## Deployment

### Environment Variables

Set the following environment variables in production:

```bash
STRIPE_SECRET_KEY=sk_live_...
STRIPE_PUBLISHABLE_KEY=pk_live_...
STRIPE_WEBHOOK_SECRET=whsec_...
STRIPE_PLATFORM_COMMISSION_PERCENTAGE=5.0
```

### Webhook Configuration

1. Configure webhook endpoint in Stripe Dashboard
2. Set webhook URL to: `https://yourdomain.com/payment/webhook`
3. Select events: `checkout.session.completed`, `account.updated`

### Database Migration

Run the database migration:

```sql
-- Migration V18__add_stripe_and_sale_fields.sql
ALTER TABLE users ADD COLUMN stripe_account_id VARCHAR(255);
ALTER TABLE users ADD COLUMN stripe_customer_id VARCHAR(255);
ALTER TABLE products ADD COLUMN sold BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE products ADD COLUMN buyer_id BIGINT;
ALTER TABLE products ADD COLUMN sold_at TIMESTAMP;
```

## Monitoring and Logging

### Key Metrics to Monitor

1. **Payment Success Rate** - Track successful vs failed payments
2. **Webhook Processing** - Monitor webhook delivery and processing
3. **Seller Onboarding** - Track onboarding completion rates
4. **Platform Revenue** - Monitor commission earnings

### Logging

The implementation includes comprehensive logging:

```java
logger.info("Payment processed successfully for session: {}", sessionId);
logger.error("Error processing webhook", e);
```

## Troubleshooting

### Common Issues

1. **Webhook Signature Verification Failed**
   - Check webhook secret configuration
   - Verify webhook URL is correct

2. **Checkout Session Creation Failed**
   - Verify Stripe keys are correct
   - Check seller account status
   - Ensure product is not already sold

3. **Payment Not Processing**
   - Check webhook endpoint is accessible
   - Verify webhook events are configured
   - Check application logs for errors

### Debug Mode

Enable debug logging:

```properties
logging.level.com.greenlink.greenlink.service.StripeService=DEBUG
logging.level.com.greenlink.greenlink.service.PaymentService=DEBUG
```

## Future Enhancements

1. **Refund Processing** - Add support for refunds
2. **Subscription Payments** - Support for recurring payments
3. **Multi-Currency** - Support for different currencies
4. **Advanced Analytics** - Detailed sales analytics and reporting
5. **Mobile SDK** - Native mobile payment integration

## Support

For issues related to:
- **Stripe Integration** - Check Stripe documentation and support
- **Application Logic** - Review application logs and error messages
- **Database Issues** - Check migration status and database connectivity

## License

This implementation is part of the GreenLink marketplace project and follows the same licensing terms. 