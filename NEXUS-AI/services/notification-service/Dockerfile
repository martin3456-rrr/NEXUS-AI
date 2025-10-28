# Etap 1: budowa aplikacji
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Etap 2: lekki runtime z użytkownikiem non-root i healthcheckiem
FROM openjdk:17-jre-slim
RUN addgroup --system spring && adduser --system --group spring

WORKDIR /app
COPY --from=builder /app/target/notification-service-*.jar app.jar
RUN chown spring:spring app.jar

USER spring:spring
EXPOSE 8085

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8085/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
