FROM openjdk:24-jre-slim

# Add a user to run the application
RUN addgroup --system spring && adduser --system --group spring

# Set working directory
WORKDIR /app

# Copy the jar file
COPY target/user-service-*.jar app.jar

# Change ownership
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring:spring

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
