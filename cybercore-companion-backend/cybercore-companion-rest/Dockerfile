# Use OpenJDK 17 as the base image
FROM openjdk:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy the jar file
COPY target/cybercore-companion-rest-1.0.0.jar app.jar

# Expose port 8080
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]