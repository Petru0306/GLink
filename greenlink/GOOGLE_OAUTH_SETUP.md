# Google OAuth2 Setup Guide for GreenLink

This guide will help you set up Google OAuth2 authentication for your GreenLink application.

## Prerequisites

1. A Google account
2. Access to Google Cloud Console
3. Your GreenLink application running locally or deployed

## Step 1: Create a Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable the Google+ API (if not already enabled)

## Step 2: Configure OAuth2 Credentials

1. In the Google Cloud Console, go to **APIs & Services** > **Credentials**
2. Click **Create Credentials** > **OAuth 2.0 Client IDs**
3. Configure the OAuth consent screen if prompted:
   - Choose **External** user type
   - Fill in the required information (App name, User support email, Developer contact information)
   - Add scopes: `openid`, `profile`, `email`
   - Add test users if needed

4. Create the OAuth 2.0 Client ID:
   - Application type: **Web application**
   - Name: `GreenLink OAuth2 Client`
   - Authorized redirect URIs:
     - For local development: `http://localhost:8080/login/oauth2/code/google`
     - For production: `https://yourdomain.com/login/oauth2/code/google`

5. Copy the **Client ID** and **Client Secret**

## Step 3: Update Application Configuration

1. Open `src/main/resources/application.properties`
2. Replace the placeholder values with your actual Google OAuth2 credentials:

```properties
# Google OAuth2 Configuration
spring.security.oauth2.client.registration.google.client-id=YOUR_ACTUAL_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_ACTUAL_CLIENT_SECRET
spring.security.oauth2.client.registration.google.scope=openid,profile,email
spring.security.oauth2.client.registration.google.redirect-uri={baseUrl}/login/oauth2/code/{registrationId}
```

## Step 4: Database Migration

The application includes a database migration to add OAuth2 fields. Make sure your database is up to date:

```sql
-- This migration is automatically applied when you start the application
-- V19__add_oauth2_fields.sql
```

## Step 5: Test the Integration

1. Start your GreenLink application
2. Navigate to the login page
3. Click the "Continuă cu Google" (Continue with Google) button
4. Complete the Google OAuth2 flow
5. You should be redirected to the dashboard after successful authentication

## Features Implemented

### User Registration & Login
- Users can register/login using their Google account
- Automatic user creation for new Google users
- Linking existing accounts to Google OAuth2
- Profile information (name, email, picture) is automatically populated

### Security Features
- Secure OAuth2 flow with proper redirect URIs
- CSRF protection maintained
- Session management
- Automatic password generation for OAuth2 users

### UI Integration
- Google OAuth2 button on login page
- Google OAuth2 button in registration modal
- Consistent green theme styling
- Responsive design

## Troubleshooting

### Common Issues

1. **"Invalid redirect URI" error**
   - Ensure the redirect URI in Google Cloud Console matches exactly
   - Check for trailing slashes or protocol mismatches

2. **"Client ID not found" error**
   - Verify the client ID and secret in application.properties
   - Ensure the OAuth2 client is properly configured in Google Cloud Console

3. **Database errors**
   - Run the database migration manually if needed
   - Check that the OAuth2 fields are added to the users table

4. **CORS issues (if applicable)**
   - Add appropriate CORS configuration for your domain
   - Ensure Google's OAuth2 endpoints are allowed

### Debug Mode

Enable debug logging for OAuth2 by adding to `application.properties`:

```properties
logging.level.org.springframework.security.oauth2=DEBUG
logging.level.org.springframework.security=DEBUG
```

## Security Considerations

1. **Client Secret**: Never commit the client secret to version control
2. **Environment Variables**: Use environment variables for production credentials
3. **HTTPS**: Always use HTTPS in production
4. **Redirect URIs**: Restrict redirect URIs to your domain only
5. **Scopes**: Only request necessary scopes (openid, profile, email)

## Production Deployment

For production deployment:

1. Update the redirect URI in Google Cloud Console to your production domain
2. Use environment variables for sensitive configuration:

```properties
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
```

3. Ensure your application is served over HTTPS
4. Configure proper session management for production

## Support

If you encounter issues:

1. Check the application logs for detailed error messages
2. Verify Google Cloud Console configuration
3. Test with a fresh browser session
4. Ensure all dependencies are properly installed

The Google OAuth2 integration is now ready to use! Users can seamlessly register and login using their Google accounts while maintaining the security and functionality of your GreenLink application. 