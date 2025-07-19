# GitHub OAuth2 Setup Guide for GreenLink

This guide will help you set up GitHub OAuth2 authentication for your GreenLink application.

## Prerequisites

1. A GitHub account
2. Your GreenLink application running locally or deployed

## Step 1: Create a GitHub OAuth App

1. Go to [GitHub Settings](https://github.com/settings)
2. Navigate to **Developer settings** > **OAuth Apps**
3. Click **New OAuth App**

## Step 2: Configure OAuth App

Fill in the following information:

### **Application name:**
```
GreenLink OAuth App
```

### **Homepage URL:**
**For local development:**
```
http://localhost:8080
```

**For production:**
```
https://glink-idie.onrender.com
```

### **Application description:**
```
GreenLink - Conectăm oamenii cu natura pentru un viitor mai verde și mai sustenabil
```

### **Authorization callback URL:**
**For local development:**
```
http://localhost:8080/login/oauth2/code/github
```

**For production:**
```
https://glink-idie.onrender.com/login/oauth2/code/github
```

## Step 3: Get OAuth Credentials

After creating the OAuth app, you'll get:

- **Client ID** (public)
- **Client Secret** (keep this secure!)

## Step 4: Set Environment Variables

### **For Local Development:**

**Windows (Command Prompt):**
```cmd
set GITHUB_CLIENT_ID=your_github_client_id
set GITHUB_CLIENT_SECRET=your_github_client_secret
```

**Windows (PowerShell):**
```powershell
$env:GITHUB_CLIENT_ID="your_github_client_id"
$env:GITHUB_CLIENT_SECRET="your_github_client_secret"
```

**Linux/Mac:**
```bash
export GITHUB_CLIENT_ID=your_github_client_id
export GITHUB_CLIENT_SECRET=your_github_client_secret
```

### **For Production (Render):**

1. Go to your Render dashboard
2. Select your GreenLink service
3. Go to **Environment** tab
4. Add these environment variables:
   - `GITHUB_CLIENT_ID` = your GitHub client ID
   - `GITHUB_CLIENT_SECRET` = your GitHub client secret

## Step 5: Test the Integration

1. Start your application
2. Go to the login page
3. Click "Continuă cu GitHub"
4. You should be redirected to GitHub for authorization
5. After authorization, you'll be redirected back to your app

## GitHub OAuth Scopes

The application requests these scopes:
- `read:user` - Read access to user profile
- `user:email` - Read access to user email addresses

## Troubleshooting

### **Common Issues:**

1. **"Invalid redirect URI" error:**
   - Make sure the callback URL in GitHub matches exactly
   - Check for trailing slashes or protocol mismatches

2. **"Client ID not found" error:**
   - Verify your Client ID is correct
   - Check that environment variables are set properly

3. **"Client secret mismatch" error:**
   - Verify your Client Secret is correct
   - Make sure there are no extra spaces or characters

### **Testing:**

- GitHub OAuth works immediately without verification
- No test users needed
- Works for any GitHub user

## Security Notes

- Keep your Client Secret secure
- Never commit it to version control
- Use environment variables in production
- Regularly rotate your Client Secret if needed

## Next Steps

After setting up GitHub OAuth:

1. Test the login flow
2. Verify user data is being stored correctly
3. Test linking existing accounts
4. Deploy to production with proper environment variables

## Support

If you encounter issues:
1. Check the application logs
2. Verify GitHub OAuth app configuration
3. Ensure environment variables are set correctly
4. Test with a different GitHub account 