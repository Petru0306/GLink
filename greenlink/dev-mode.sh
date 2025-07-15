#!/bin/bash
echo "Starting GreenLink in local Docker development mode..."

# Stop any running containers
echo "Stopping any running containers..."
docker-compose down

# Build and start containers
echo "Building and starting containers..."
docker-compose up -d

# Show logs
echo "Container logs (press Ctrl+C to exit logs, containers will keep running):"
docker-compose logs -f

# Note: After exiting logs with Ctrl+C, containers will still be running
# To stop containers: docker-compose down