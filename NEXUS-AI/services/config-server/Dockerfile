# Etap 1: Budowanie
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Etap 2: Tworzenie lekkiego obrazu
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
# Skopiuj tylko JAR z etapu budowania
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
# Ustaw punkt wejścia
ENTRYPOINT ["java", "-jar", "app.jar"]