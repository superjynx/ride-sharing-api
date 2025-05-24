# Production Environment Setup Script

# MongoDB Configuration
$env:MONGODB_URI="mongodb://localhost:27017/ridesharing-prod"
$env:MONGODB_USERNAME="admin"
$env:MONGODB_PASSWORD="your-secure-password"

# JWT Configuration
$env:JWT_SECRET="your-production-jwt-secret-key-min-32-chars"
$env:JWT_EXPIRATION="86400000" # 24 hours in milliseconds

# Server Configuration
$env:PORT="8080"
$env:ALLOWED_ORIGINS="https://your-frontend-domain.com"

# SSL Configuration
$env:SSL_KEYSTORE_PATH="config/ridesharing.p12"
$env:SSL_KEYSTORE_PASSWORD="your-keystore-password"

# Logging Configuration
$env:LOG_LEVEL="INFO"

Write-Host "Production environment variables have been set."
Write-Host "Please make sure to update the values with your actual production settings." 