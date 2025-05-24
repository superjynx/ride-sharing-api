# Deployment Script

# Build the application
Write-Host "Building the application..."
./gradlew clean build -x test

# Create deployment directory
$deployDir = "deploy"
New-Item -ItemType Directory -Force -Path $deployDir

# Copy necessary files
Copy-Item "build/libs/*.jar" -Destination $deployDir
Copy-Item "config" -Destination $deployDir -Recurse
Copy-Item "application-prod.properties" -Destination $deployDir

# Create startup script
$startupScript = @"
@echo off
set JAVA_OPTS=-Xms512m -Xmx1024m
java -jar ridesharing-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
"@

$startupScript | Out-File -FilePath "$deployDir\start.bat" -Encoding ascii

Write-Host "Deployment package created in $deployDir"
Write-Host "To deploy:"
Write-Host "1. Copy the contents of $deployDir to your production server"
Write-Host "2. Set up environment variables using setup-prod-env.ps1"
Write-Host "3. Run start.bat to start the application" 