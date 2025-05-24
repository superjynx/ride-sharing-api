# MongoDB Production Setup Script

# Create MongoDB data directory
$dataDir = "mongodb-data"
New-Item -ItemType Directory -Force -Path $dataDir

# Create MongoDB configuration file
$configContent = @"
systemLog:
  destination: file
  path: $dataDir\mongod.log
  logAppend: true
storage:
  dbPath: $dataDir
  journal:
    enabled: true
net:
  bindIp: 127.0.0.1
  port: 27017
security:
  authorization: enabled
"@

$configContent | Out-File -FilePath "mongod.conf" -Encoding utf8

Write-Host "MongoDB configuration file created."
Write-Host "To start MongoDB with this configuration, run:"
Write-Host "mongod --config mongod.conf"
Write-Host ""
Write-Host "After starting MongoDB, create an admin user:"
Write-Host "mongosh"
Write-Host "use admin"
Write-Host "db.createUser({"
Write-Host "  user: 'admin',"
Write-Host "  pwd: 'your-secure-password',"
Write-Host "  roles: [ { role: 'userAdminAnyDatabase', db: 'admin' } ]"
Write-Host "})" 