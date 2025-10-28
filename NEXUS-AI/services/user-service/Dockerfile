# Etap 1: budowa aplikacji
FROM openjdk:21-jdk-jammy AS builder
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Etap 2: lekkie uruchomienie
FROM openjdk:21-jre-slim
WORKDIR /app

# Dodanie użytkownika do uruchamiania aplikacji
RUN addgroup --system spring && adduser --system --ingroup spring spring

# Skopiuj JAR z etapu budowania
COPY --from=builder /app/target/*.jar app.jar

# Zmień własność
RUN chown spring:spring app.jar

# Użytkownik bez uprawnień root
USER spring:spring

# Ekspozycja portu
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

# Start aplikacji
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
