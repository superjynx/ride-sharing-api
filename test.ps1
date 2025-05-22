# Test user registration
$headers = @{
    "Content-Type" = "application/json"
}

# Register a driver
$driverBody = @{
    username = "testdriver"
    password = "password123"
    role = "ROLE_DRIVER"
    fullName = "Test Driver"
    email = "testdriver@example.com"
} | ConvertTo-Json

Write-Host "Registering driver..."
try {
    $driverResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/signup" -Method Post -Headers $headers -Body $driverBody
    Write-Host "Driver registration response: $($driverResponse | ConvertTo-Json)"
} catch { Write-Host "Driver registration failed (likely already exists)" }

# Register a student
$studentBody = @{
    username = "teststudent"
    password = "password123"
    role = "ROLE_STUDENT"
    fullName = "Test Student"
    email = "teststudent@example.com"
} | ConvertTo-Json

Write-Host "Registering student..."
try {
    $studentResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/signup" -Method Post -Headers $headers -Body $studentBody
    Write-Host "Student registration response: $($studentResponse | ConvertTo-Json)"
} catch { Write-Host "Student registration failed (likely already exists)" }

# Login as driver
$driverLoginBody = @{
    username = "testdriver"
    password = "password123"
} | ConvertTo-Json

Write-Host "Logging in as driver..."
$driverToken = (Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Headers $headers -Body $driverLoginBody).token
Write-Host "Driver token: $driverToken"

# Login as student
$studentLoginBody = @{
    username = "teststudent"
    password = "password123"
} | ConvertTo-Json

Write-Host "Logging in as student..."
$studentToken = (Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Headers $headers -Body $studentLoginBody).token
Write-Host "Student token: $studentToken"

# Create a ride (as driver)
$rideBody = @{
    origin = "Campus A"
    destination = "Campus B"
    departureTime = (Get-Date).AddHours(1).ToString("yyyy-MM-ddTHH:mm:ss.fffffff")
    availableSeats = 2
    maxPassengers = 2
    price = 10.00
    campusLocation = "North Campus"
    buildingName = "Engineering Building"
    scheduleType = "ONE_TIME"
    vehicleType = "Sedan"
    vehicleNumber = "ABC123"
    isCarpool = $false
    notes = "Test ride"
} | ConvertTo-Json

$driverHeaders = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $driverToken"
}

Write-Host "Creating ride as driver..."
$rideResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/rides" -Method Post -Headers $driverHeaders -Body $rideBody
Write-Host "Ride creation response: $($rideResponse | ConvertTo-Json)"
$rideId = $rideResponse.id

# Book a ride (as student)
$studentHeaders = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $studentToken"
}

Write-Host "Booking ride as student..."
$bookingResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/rides/$rideId/book" -Method Post -Headers $studentHeaders
Write-Host "Booking response: $($bookingResponse | ConvertTo-Json)"

# Try double booking (should fail)
Write-Host "Attempting to double book as student (should fail)..."
try {
    $doubleBooking = Invoke-RestMethod -Uri "http://localhost:8080/api/rides/$rideId/book" -Method Post -Headers $studentHeaders
    Write-Host "Double booking response: $($doubleBooking | ConvertTo-Json)"
} catch { Write-Host "Double booking failed as expected: $($_.Exception.Message)" }

# Register a second student
$student2Body = @{
    username = "teststudent2"
    password = "password123"
    role = "ROLE_STUDENT"
    fullName = "Test Student2"
    email = "teststudent2@example.com"
} | ConvertTo-Json

Write-Host "Registering second student..."
try {
    $student2Response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/signup" -Method Post -Headers $headers -Body $student2Body
    Write-Host "Second student registration response: $($student2Response | ConvertTo-Json)"
} catch { Write-Host "Second student registration failed (likely already exists)" }

# Login as second student
$student2LoginBody = @{
    username = "teststudent2"
    password = "password123"
} | ConvertTo-Json
$student2Token = (Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Headers $headers -Body $student2LoginBody).token
$student2Headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $student2Token"
}

# Book as second student (should succeed, ride now full)
Write-Host "Booking ride as second student..."
$booking2Response = Invoke-RestMethod -Uri "http://localhost:8080/api/rides/$rideId/book" -Method Post -Headers $student2Headers
Write-Host "Second student booking response: $($booking2Response | ConvertTo-Json)"

# Try booking as third student (should fail, ride full)
$student3Body = @{
    username = "teststudent3"
    password = "password123"
    role = "ROLE_STUDENT"
    fullName = "Test Student3"
    email = "teststudent3@example.com"
} | ConvertTo-Json
Write-Host "Registering third student..."
try {
    $student3Response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/signup" -Method Post -Headers $headers -Body $student3Body
    Write-Host "Third student registration response: $($student3Response | ConvertTo-Json)"
} catch { Write-Host "Third student registration failed (likely already exists)" }
$student3LoginBody = @{
    username = "teststudent3"
    password = "password123"
} | ConvertTo-Json
$student3Token = (Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Headers $headers -Body $student3LoginBody).token
$student3Headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $student3Token"
}
Write-Host "Booking ride as third student (should fail, ride full)..."
try {
    $booking3Response = Invoke-RestMethod -Uri "http://localhost:8080/api/rides/$rideId/book" -Method Post -Headers $student3Headers
    Write-Host "Third student booking response: $($booking3Response | ConvertTo-Json)"
} catch { Write-Host "Third student booking failed as expected: $($_.Exception.Message)" }

# Try booking as driver (should fail)
Write-Host "Booking ride as driver (should fail)..."
try {
    $driverBooking = Invoke-RestMethod -Uri "http://localhost:8080/api/rides/$rideId/book" -Method Post -Headers $driverHeaders
    Write-Host "Driver booking response: $($driverBooking | ConvertTo-Json)"
} catch { Write-Host "Driver booking failed as expected: $($_.Exception.Message)" }

# Try creating a ride as student (should fail)
Write-Host "Creating ride as student (should fail)..."
try {
    $studentRide = Invoke-RestMethod -Uri "http://localhost:8080/api/rides" -Method Post -Headers $studentHeaders -Body $rideBody
    Write-Host "Student ride creation response: $($studentRide | ConvertTo-Json)"
} catch { Write-Host "Student ride creation failed as expected: $($_.Exception.Message)" }

# Try accessing protected endpoint without token (should fail)
Write-Host "Accessing protected endpoint without token (should fail)..."
try {
    $noAuth = Invoke-RestMethod -Uri "http://localhost:8080/api/rides/$rideId" -Method Get
    Write-Host "No-auth response: $($noAuth | ConvertTo-Json)"
} catch { Write-Host "No-auth access failed as expected: $($_.Exception.Message)" }

# Student unbooks ride
Write-Host "Student unbooking ride..."
try {
    $unbook = Invoke-RestMethod -Uri "http://localhost:8080/api/rides/$rideId/unbook" -Method Post -Headers $studentHeaders
    Write-Host "Unbook response: $($unbook | ConvertTo-Json)"
} catch { Write-Host "Unbook failed: $($_.Exception.Message)" }

# Driver cancels ride
Write-Host "Driver cancelling ride..."
try {
    $cancel = Invoke-RestMethod -Uri "http://localhost:8080/api/rides/$rideId/cancel" -Method Post -Headers $driverHeaders
    Write-Host "Cancel response: $($cancel | ConvertTo-Json)"
} catch { Write-Host "Cancel failed: $($_.Exception.Message)" }

# Get my rides as driver
Write-Host "Getting my rides as driver..."
$myRides = Invoke-RestMethod -Uri "http://localhost:8080/api/rides/my-rides" -Method Get -Headers $driverHeaders
Write-Host "My rides (driver): $($myRides | ConvertTo-Json)"

# Get my bookings as student
Write-Host "Getting my bookings as student..."
$myBookings = Invoke-RestMethod -Uri "http://localhost:8080/api/rides/my-bookings" -Method Get -Headers $studentHeaders
Write-Host "My bookings (student): $($myBookings | ConvertTo-Json)" 