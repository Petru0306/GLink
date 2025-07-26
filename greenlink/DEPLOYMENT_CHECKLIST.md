# 🚀 Render Deployment Checklist

## ✅ Pre-Deployment Checklist

### 1. **Environment Variables Setup**
- [ ] **Stripe Configuration**
  - [ ] `STRIPE_SECRET_KEY` (Live key for production)
  - [ ] `STRIPE_PUBLISHABLE_KEY` (Live key for production)
  - [ ] `STRIPE_WEBHOOK_SECRET` (Production webhook secret)

- [ ] **OAuth2 Configuration**
  - [ ] `GOOGLE_CLIENT_ID` (Production client ID)
  - [ ] `GOOGLE_CLIENT_SECRET` (Production client secret)
  - [ ] `GITHUB_CLIENT_ID` (Production client ID)
  - [ ] `GITHUB_CLIENT_SECRET` (Production client secret)

- [ ] **reCAPTCHA Configuration**
  - [ ] `RECAPTCHA_SITE_KEY` (Production site key)
  - [ ] `RECAPTCHA_SECRET_KEY` (Production secret key)

### 2. **External Service Configuration**

#### **Stripe Dashboard**
- [ ] Switch to **LIVE** mode
- [ ] Update webhook endpoint to: `https://greenlink.onrender.com/payment/webhook`
- [ ] Select events: `checkout.session.completed`, `account.updated`
- [ ] Copy production webhook secret

#### **Google OAuth2**
- [ ] Go to Google Cloud Console
- [ ] Add production redirect URI: `https://greenlink.onrender.com/login/oauth2/code/google`
- [ ] Copy production client ID and secret

#### **GitHub OAuth2**
- [ ] Go to GitHub Developer Settings
- [ ] Add production redirect URI: `https://greenlink.onrender.com/login/oauth2/code/github`
- [ ] Copy production client ID and secret

#### **Google reCAPTCHA**
- [ ] Go to Google reCAPTCHA Admin
- [ ] Add domain: `greenlink.onrender.com`
- [ ] Copy production site key and secret key

### 3. **Database Migration**
- [ ] Ensure all Flyway migrations are ready
- [ ] Test migrations locally with PostgreSQL
- [ ] Backup existing data if migrating from old system

### 4. **Code Review**
- [ ] All critical bugs fixed (payment, navigation, JavaScript)
- [ ] Production profile configuration created
- [ ] Debug logging disabled in production
- [ ] Security headers configured
- [ ] File upload paths configured correctly

## 🚀 Deployment Steps

### 1. **Render Dashboard Setup**
1. Go to [Render Dashboard](https://dashboard.render.com/)
2. Click "New +" → "Blueprint"
3. Connect your Git repository
4. Review the `render.yaml` configuration
5. Click "Apply" to create resources

### 2. **Environment Variables Configuration**
1. Go to your web service in Render
2. Navigate to "Environment" tab
3. Add all required environment variables:
   ```
   STRIPE_SECRET_KEY=sk_live_...
   STRIPE_PUBLISHABLE_KEY=pk_live_...
   STRIPE_WEBHOOK_SECRET=whsec_...
   GOOGLE_CLIENT_ID=...
   GOOGLE_CLIENT_SECRET=...
   GITHUB_CLIENT_ID=...
   GITHUB_CLIENT_SECRET=...
   RECAPTCHA_SITE_KEY=...
   RECAPTCHA_SECRET_KEY=...
   ```

### 3. **Database Setup**
1. Wait for PostgreSQL database to be created
2. Verify database connection in Render logs
3. Check Flyway migrations are applied successfully

### 4. **Service Verification**
1. Wait for build to complete
2. Check application logs for any errors
3. Verify application is accessible at the provided URL
4. Test basic functionality (login, registration)

## 🔍 Post-Deployment Testing

### 1. **Core Functionality**
- [ ] User registration and login
- [ ] OAuth2 login (Google, GitHub)
- [ ] Marketplace browsing
- [ ] Product listing and selling
- [ ] Payment processing with Stripe
- [ ] File uploads (product images)

### 2. **Payment System**
- [ ] Seller onboarding with Stripe Connect
- [ ] Product purchase flow
- [ ] Webhook processing
- [ ] Payment success/cancel pages

### 3. **Security**
- [ ] HTTPS redirects working
- [ ] CSRF protection active
- [ ] Session management working
- [ ] File upload security

### 4. **Performance**
- [ ] Page load times acceptable
- [ ] Database queries optimized
- [ ] Static resources cached
- [ ] Image loading working

## 🐛 Troubleshooting

### Common Issues

1. **Build Failures**
   - Check Maven dependencies
   - Verify Java version compatibility
   - Check for compilation errors

2. **Database Connection Issues**
   - Verify database credentials
   - Check network connectivity
   - Ensure Flyway migrations run

3. **Payment Issues**
   - Verify Stripe keys are live (not test)
   - Check webhook endpoint is accessible
   - Verify domain configuration

4. **OAuth2 Issues**
   - Check redirect URIs match exactly
   - Verify client IDs and secrets
   - Check domain configuration

### Monitoring

1. **Application Logs**
   - Monitor for errors in Render dashboard
   - Check for performance issues
   - Watch for security events

2. **Database Monitoring**
   - Monitor connection pool usage
   - Check for slow queries
   - Watch for migration issues

3. **External Services**
   - Monitor Stripe webhook delivery
   - Check OAuth2 callback success rates
   - Monitor reCAPTCHA success rates

## 📞 Support

If you encounter issues:
1. Check Render application logs
2. Verify all environment variables are set
3. Test external service configurations
4. Review this checklist for missed steps

## 🎉 Success Criteria

Your deployment is successful when:
- [ ] Application is accessible at `https://greenlink.onrender.com`
- [ ] All core features work correctly
- [ ] Payment system processes transactions
- [ ] OAuth2 login works for both providers
- [ ] File uploads work properly
- [ ] No critical errors in logs
- [ ] Performance is acceptable

---

**Last Updated**: $(date)
**Version**: 1.0 