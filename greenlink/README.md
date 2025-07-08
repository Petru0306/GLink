# GreenLink

GreenLink is a Spring Boot application for promoting eco-friendly products and sustainable living.

## Deploying to Render.com

### Prerequisites

1. A [Render.com](https://render.com/) account
2. Your code pushed to a Git repository (GitHub, GitLab, or Bitbucket)

### Deployment Steps

#### Option 1: Using the Dashboard (Manual)

1. **Log in** to your Render.com account
2. **Create a new PostgreSQL database**:
   - Click on "New +" and select "PostgreSQL"
   - Name it "greenlink-db"
   - Choose the Free plan
   - Set the database name to "glink"
   - Click "Create Database" and wait for it to be provisioned

3. **Create a new Web Service**:
   - Click on "New +" and select "Web Service"
   - Connect your Git repository
   - Name the service "greenlink"
   - Select "Docker" as the runtime
   - Choose the Free plan
   - Set the following environment variables:
     - `SPRING_PROFILES_ACTIVE`: `prod`
     - `JDBC_DATABASE_URL`: Copy from your PostgreSQL database's "Connection String"
     - `JDBC_DATABASE_USERNAME`: Copy from your PostgreSQL database's "User"
     - `JDBC_DATABASE_PASSWORD`: Copy from your PostgreSQL database's "Password"
   - Click "Create Web Service"

4. **Wait for Deployment**:
   - Render will build and deploy your application
   - Once deployed, you can access your application at the provided URL

#### Option 2: Using Blueprint (Automated)

1. **Add your repository to Render**:
   - Click on "New +" and select "Blueprint"
   - Connect your Git repository
   - Render will automatically detect the `render.yaml` file
   - Review the resources to be created
   - Click "Apply"

2. **Wait for Deployment**:
   - Render will create the database and web service
   - Once deployed, you can access your application at the provided URL

### Troubleshooting

- **Check Logs**: If your application fails to start, check the logs in the Render dashboard
- **Database Connection**: Ensure the database connection details are correct
- **File Uploads**: Note that file uploads will be lost when the container restarts on the free plan; consider using an external storage service for production

## Local Development

1. Clone the repository
2. Set up a PostgreSQL database named "glink"
3. Run `./mvnw spring-boot:run`
4. Access the application at http://localhost:8080 