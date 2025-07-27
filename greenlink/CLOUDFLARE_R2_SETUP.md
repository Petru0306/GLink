# CloudFlare R2 Integration Setup

This guide explains how to set up CloudFlare R2 for image storage in the GreenLink marketplace.

## Prerequisites

1. A CloudFlare account
2. Access to CloudFlare R2 Object Storage

## Step 1: Create R2 Bucket

1. Log in to your CloudFlare dashboard
2. Navigate to **R2 Object Storage**
3. Click **Create bucket**
4. Name your bucket: `product-images`
5. Choose your preferred region
6. Click **Create bucket**

## Step 2: Create Account API Token

1. In your CloudFlare dashboard, go to **My Profile** > **API Tokens**
2. Click **Create Token**
3. Choose **Custom token**
4. Configure the token with the following permissions:
   - **Account** > **Cloudflare R2 Storage** > **Object Read & Write**
   - **Zone** > **Zone** > **Read** (if needed for custom domains)
5. Set the **Account Resources** to include your account
6. **IP Address Filtering** (Optional - for additional security):
   - Add your server's IP address(es) to restrict access
   - For production: Add your hosting provider's IP ranges
   - For development: Add your local IP address
   - **Leave empty to allow access from anywhere** (simpler setup)
7. Click **Continue to summary** and then **Create Token**
8. **Important**: Copy and save the token - you won't be able to see it again!

**Note**: You need an **Account API Token**, not a User API Token, because R2 operations require account-level permissions.

**Permission Level**: Choose **"Object Read & Write"** - this gives you the minimum required permissions to upload, read, and delete images in your bucket without admin access.

## Step 3: Get Account ID

1. In your CloudFlare dashboard, look at the URL or sidebar
2. Your Account ID is displayed in the format: `xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`
3. Copy this ID

## Step 3.5: Find Your IP Addresses

### For Development (Local Testing)
```bash
# On Windows
ipconfig

# On Mac/Linux
curl ifconfig.me
# or
curl ipinfo.io/ip
```

### For Production (Hosting Providers)
- **Render**: Check your app's logs or dashboard for outbound IP
- **Heroku**: Use `curl ifconfig.me` from your app or check dyno IP
- **AWS**: Use your EC2 instance's public IP or Elastic IP
- **Google Cloud**: Use your VM's external IP
- **Azure**: Use your VM's public IP

### IP Range Examples
- Single IP: `192.168.1.100`
- IP Range: `192.168.1.0/24`
- Multiple IPs: `192.168.1.100, 10.0.0.50`

## Step 4: Configure Environment Variables

Set the following environment variables in your deployment environment:

```bash
# Required for R2 to work
CLOUDFLARE_R2_ENABLED=true
CLOUDFLARE_R2_ACCOUNT_ID=your_account_id_here
CLOUDFLARE_R2_ACCESS_KEY_ID=your_access_key_id_here
CLOUDFLARE_R2_SECRET_ACCESS_KEY=your_secret_access_key_here

# Optional (defaults shown)
CLOUDFLARE_R2_BUCKET_NAME=product-images
CLOUDFLARE_R2_REGION=auto
```

## Step 5: Create R2 API Keys

1. In your CloudFlare dashboard, go to **R2 Object Storage**
2. Click on your `product-images` bucket
3. Go to **Manage R2 API tokens**
4. Click **Create API token**
5. Choose **Custom token**
6. Set permissions:
   - **Object Read** - Allow
   - **Object Write** - Allow
   - **Object Delete** - Allow
7. Set **TTL** to your preference (or leave as default)
8. Click **Create API token**
9. Copy the **Access Key ID** and **Secret Access Key**

**Important**: These are the credentials you'll use in your environment variables:
- `CLOUDFLARE_R2_ACCESS_KEY_ID` = Access Key ID
- `CLOUDFLARE_R2_SECRET_ACCESS_KEY` = Secret Access Key
- `CLOUDFLARE_R2_ENDPOINT` = Jurisdiction-specific endpoint (use this instead of the default)

**Note**: The Token Value is for administrative access and may not be needed for basic R2 operations.

## Step 6: Configure Public Access (Optional)

If you want to serve images directly from R2 without going through your application:

1. In your R2 bucket settings, go to **Settings** > **Public URL**
2. Enable **Public URL**
3. Your images will be accessible at: `https://pub-product-images.your-account-id.r2.dev/filename`

## Step 7: Test the Integration

1. Start your application with the environment variables set
2. Try uploading a product image through the marketplace
3. Check that the image is stored in your R2 bucket
4. Verify the image URL is correctly generated

## Fallback Behavior

The system is designed to gracefully fall back to local storage when R2 is not configured:

- If `CLOUDFLARE_R2_ENABLED=false` or not set, images will be stored locally
- If R2 credentials are missing, the system will use local storage
- Existing local images will continue to work

## Troubleshooting

### Common Issues

1. **"Access Denied" errors**: Check your API token permissions
2. **"Bucket not found"**: Verify the bucket name and account ID
3. **"Invalid credentials"**: Ensure your access key and secret are correct

### Debug Mode

Enable debug logging by adding to your `application.properties`:

```properties
logging.level.com.greenlink.greenlink.service.R2StorageService=DEBUG
logging.level.software.amazon.awssdk=DEBUG
```

### Local Development

For local development, you can disable R2 and use local storage:

```properties
cloudflare.r2.enabled=false
```

## Security Considerations

1. **Never commit API keys to version control**
2. **Use environment variables for all sensitive data**
3. **Rotate API keys regularly**
4. **Limit API token permissions to minimum required**
5. **Use IP address filtering** to restrict access to your servers only
6. **Monitor API usage** in CloudFlare dashboard
7. **Consider using CloudFlare Workers for additional security**

## Cost Optimization

1. **Monitor your R2 usage** in the CloudFlare dashboard
2. **Set up billing alerts** to avoid unexpected charges
3. **Consider image optimization** before upload to reduce storage costs
4. **Implement cleanup policies** for unused images

## Migration from Local Storage

If you're migrating from local storage to R2:

1. **Backup existing images** before migration
2. **Update existing product records** to use R2 URLs
3. **Test thoroughly** in a staging environment
4. **Monitor for any broken image links** after migration 