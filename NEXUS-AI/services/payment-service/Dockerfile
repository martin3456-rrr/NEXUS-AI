FROM openjdk:17-jre-slim

RUN addgroup --system spring && adduser --system --group spring

WORKDIR /app

COPY target/payment-service-*.jar app.jar

RUN chown spring:spring app.jar

USER spring:spring

EXPOSE 8086

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8086/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
