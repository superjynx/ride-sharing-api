# SSL Certificate Generation Script

# Create config directory if it doesn't exist
New-Item -ItemType Directory -Force -Path "config"

# Generate SSL certificate
$keystorePath = "config/ridesharing.p12"
$keystorePassword = "your-keystore-password"
$alias = "ridesharing"
$validity = 3650 # 10 years

Write-Host "Generating SSL certificate..."
keytool -genkeypair `
    -alias $alias `
    -keyalg RSA `
    -keysize 2048 `
    -storetype PKCS12 `
    -keystore $keystorePath `
    -validity $validity `
    -storepass $keystorePassword

Write-Host "SSL certificate generated successfully at $keystorePath"
Write-Host "Please update the keystore password in your environment variables." 