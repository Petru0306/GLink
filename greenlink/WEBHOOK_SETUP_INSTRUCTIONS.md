# Stripe Webhook Setup Instructions

## 1. Access Stripe Dashboard
1. Go to https://dashboard.stripe.com/
2. Make sure you're in TEST mode (toggle in top-left)
3. Navigate to "Developers" → "Webhooks"

## 2. Create Webhook Endpoint
1. Click "Add endpoint"
2. Enter endpoint URL: `https://yourdomain.com/payment/webhook`
   - Replace `yourdomain.com` with your actual domain
   - For local testing: `https://yourlocalurl.ngrok.io/payment/webhook`
3. Select events to listen for:
   - `checkout.session.completed`
   - `account.updated`
4. Click "Add endpoint"

## 3. Get Webhook Secret
1. Click on your newly created webhook
2. Click "Reveal" next to "Signing secret"
3. Copy the secret (starts with `whsec_`)
4. Update your `application.properties`:
   ```
   stripe.webhook-secret=whsec_your_actual_webhook_secret_here
   ```

## 4. Test Webhook (Optional)
1. Use Stripe CLI: `stripe listen --forward-to localhost:8080/payment/webhook`
2. This will give you a test webhook secret for local development

## 5. Production Setup
For production, repeat the same steps but:
1. Use your production domain
2. Switch to LIVE mode in Stripe Dashboard
3. Use live API keys and webhook secret
