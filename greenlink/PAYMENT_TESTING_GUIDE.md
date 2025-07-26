# Stripe Payment Testing Guide

## Testing Checklist

### Prerequisites
- ✅ Webhooks configured in Stripe Dashboard  
- ✅ Application running with correct Stripe keys
- ✅ Database migrations applied (V18)

### 1. Test Seller Onboarding
1. **Create a seller account** or use existing user
2. **Navigate to seller onboarding**: `/payment/onboard-seller`
3. **Complete Stripe Connect onboarding**
4. **Verify account status**: `/payment/account-status`
5. **Expected result**: Seller has `stripe_account_id` in database

### 2. Test Product Purchase Flow
1. **Login as a buyer** (different from seller)
2. **Navigate to product details page**
3. **Click "Buy Now" button**
4. **Expected**: Redirect to Stripe Checkout
5. **Use test card**: `4242 4242 4242 4242`
   - Expiry: Any future date
   - CVC: Any 3 digits
   - ZIP: Any 5 digits
6. **Complete payment**
7. **Expected**: Redirect to success page
8. **Verify**: Product marked as sold in database

### 3. Test Webhook Processing
1. **Check webhook delivery** in Stripe Dashboard
2. **Check application logs** for webhook processing
3. **Verify database**: Product should be marked as sold
4. **Verify database**: Buyer should be associated with product

### 4. Test Error Scenarios
1. **Try to buy already sold product**
2. **Try to buy own product** (as seller)
3. **Try to buy without Stripe customer** (should create one)
4. **Try to buy from seller without onboarding** (should fail)

## Test Cards (Stripe Test Mode)
- **Successful payment**: `4242 4242 4242 4242`
- **Requires authentication**: `4000 0025 0000 3155`
- **Declined**: `4000 0000 0000 0002`
- **Insufficient funds**: `4000 0000 0000 9995`

## Common Issues & Solutions

### Issue: "Seller must complete Stripe onboarding"
**Solution**: Seller needs to complete onboarding flow first

### Issue: Webhook not receiving events
**Solution**: 
1. Check webhook URL is correct
2. Verify webhook secret matches
3. Check if webhook is active in Stripe Dashboard

### Issue: Payment succeeds but product not marked as sold
**Solution**: 
1. Check webhook is processing correctly
2. Check application logs for errors
3. Verify webhook signature validation

## Monitoring & Logs
- **Stripe Dashboard**: Monitor payments and webhooks
- **Application logs**: Check payment processing
- **Database**: Verify data changes
- **Network tab**: Check API calls in browser
