# Campus Ridesharing Application

A Spring Boot-based ridesharing application designed for university campuses, allowing students and drivers to coordinate rides efficiently.

## Features

- User authentication with JWT
- Role-based access control (Student/Driver)
- Ride creation and management
- Booking system with seat management
- Real-time notifications
- Ride status tracking
- Automated reminders
- Data retention policies

## Prerequisites

- Java 17 or higher
- MongoDB 4.4 or higher
- Gradle 8.x
- Node.js and npm (for frontend)

## Environment Setup

1. Install MongoDB:
   - Download and install MongoDB from [MongoDB website](https://www.mongodb.com/try/download/community)
   - Start MongoDB service
   - Default port: 27017

2. Configure Environment Variables:
   Create `application-dev.properties` for development:
   ```properties
   spring.data.mongodb.uri=mongodb://localhost:27017/ridesharing-dev
   jwt.secret=your-development-secret-key
   jwt.expiration=3600000
   ```

   Create `application-prod.properties` for production:
   ```properties
   spring.data.mongodb.uri=${MONGODB_URI}
   jwt.secret=${JWT_SECRET}
   jwt.expiration=${JWT_EXPIRATION}
   ```

## Building and Running

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/campus-ridesharing.git
   cd campus-ridesharing
   ```

2. Build the application:
   ```bash
   ./gradlew build
   ```

3. Run the application:
   ```bash
   ./gradlew bootRun
   ```

The application will start on port 8080 by default.

## API Documentation

### Authentication Endpoints

- POST `/api/auth/signup` - Register new user
- POST `/api/auth/login` - Login and get JWT token

### Ride Endpoints

- POST `/api/rides` - Create new ride (Driver only)
- GET `/api/rides` - List available rides
- GET `/api/rides/{id}` - Get ride details
- POST `/api/rides/{id}/book` - Book a ride (Student only)
- POST `/api/rides/{id}/unbook` - Cancel booking
- POST `/api/rides/{id}/cancel` - Cancel ride (Driver only)
- GET `/api/rides/my-rides` - Get user's created rides (Driver only)
- GET `/api/rides/my-bookings` - Get user's booked rides (Student only)

## Security Considerations

1. JWT Secret:
   - Change the default JWT secret in production
   - Use environment variables for sensitive data
   - Rotate secrets periodically

2. MongoDB:
   - Enable authentication in production
   - Use SSL/TLS for connections
   - Regular backups

3. Application:
   - Enable HTTPS in production
   - Implement rate limiting
   - Regular security updates

## Testing

Run the test suite:
```bash
./gradlew test
```

Run integration tests:
```bash
./gradlew integrationTest
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details. 