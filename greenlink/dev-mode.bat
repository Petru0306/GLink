@echo off
echo Starting GreenLink in local Docker development mode...

REM Stop any running containers
echo Stopping any running containers...
docker-compose down

REM Build and start containers
echo Building and starting containers...
docker-compose up -d

REM Show logs
echo Container logs (press Ctrl+C to exit logs, containers will keep running):
docker-compose logs -f

REM Note: After exiting logs with Ctrl+C, containers will still be running
REM To stop containers: docker-compose down