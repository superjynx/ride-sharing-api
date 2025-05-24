# Use a Java 17 base image
FROM eclipse-temurin:17-jdk

# Set the working directory inside the container
WORKDIR /app

# Copy everything from your project into the container
COPY . .

# Make gradlew executable
RUN chmod +x gradlew

# Build the Spring Boot app
RUN ./gradlew build --no-daemon

# Expose the port your Spring Boot app runs on
EXPOSE 8080

# Run the built JAR (update with your actual JAR file name)
CMD ["java", "-jar", "build/libs/ridesharing-0.0.1-SNAPSHOT.jar"]
