# NEXUS AI - Java and Spring Boot Microservices Platform

## Project Description
The project is a comprehensive, event-driven microservices platform written in Java and Spring Boot.
It combines AI analytics, payment processing, blockchain integration, and real-time notifications.
The project is currently under development.

## Architecture
- **API Gateway:** central entry point, routing and load balancing, rate limiting with Redis
- **Eureka Server:** service discovery
- **Config Server:** central configuration management with a Git repository
- **User Service:** user management, JWT, registration, login
- **AI Analytics Service:** LSTM model for forecasting
- **Payment Service:** handling Stripe payments and publishing Kafka events
- **Notification Service:** handling notifications via Kafka and WebSocket
- **Blockchain Service:** simple, proprietary implementation with PoS
- **Voting Service:** integration with the Ethereum blockchain

## Technologies
- Spring Boot, Spring Cloud, Spring Security
- Deeplearning4j (LSTM)
- PostgreSQL, Redis, Kafka, WebSocket (STOMP)
- Stripe SDK, Web3j
- JUnit 5 Mockito, Testcontainers
- Docker, Maven

## Startup

### Requirements
- Java 25
- Maven 3.8+
- Docker and Docker Compose
- Git

### Instructions
1. Start Config Server with secrets as environment variables. 
 ```bash
 docker run -d -p 8888:
-e "STRIPE_SECRET_KEY=sk_live_..."
-e "INFURA_URL=https://sepolia.infura.io/v3/..."
-e "ETH_PRIVATE_KEY=0x..."
-e "ETH_CONTRACT_ADDRESS=0x..."
nexus/config-server
```

2. Start the infrastructure:
```bash
docker-compose up -d eureka-server kafka postgres redis
```
3. Start the microservices (example for AI Analytics):
```bash
cd ai-analytics-service
mvn spring-boot:run
```

## MONITORING AND ALERTING (RUNBOOK)
- **Metrics Stack:** Prometheus, Grafana, and Alertmanager.

- **Health Checks:** Every microservice exposes standard Spring Boot health checks:

- **Health Check:** /actuator/health (Liveness/Readiness Probes for K8s)

- **Metrics:** /actuator/prometheus (Metrics target)

- **Key Alerts and Response Procedures:**

- **Alert:** JvmHeapUsageHigh

- **Condition:** Heap memory usage > 80% for 5m.

- **Action:** Check logs for memory leaks or high load in the reported service ({{$labels.job}}). Primary Fix: Scale out the service (add more replicas) or increase the JVM memory limit in the service's K8s Deployment YAML.

- **Alert:** HighHttp5xxErrors

- **Condition:** Service returning 5xx errors for 2m.

- **Action:** This indicates a downstream issue (internal server error). Immediate Action: Check logs of the reported service and its dependencies (e.g., PostgreSQL, Kafka, Redis).

- **Alert:** HighRequestLatency

- **Condition:** 95th percentile of request latency > 1.5s for 5m.

- **Action:** Investigate service performance. If the service is CPU-bound (e.g., AI-Analytics-Service), scale out replicas. If it's IO-bound, check database performance or external API latency (Stripe, Infura).

## KUBERNETES DEPLOYMENT & SECRETS MANAGEMENT
- **Deployment Context:** All Kubernetes manifests (Deployments, Services, Ingress) are located in the k8s/ directory, targeting the nexus-ai namespace.

**CRITICAL SETUP: Creating Secrets:**

The system requires the following secrets to be created in the cluster before deployment. These commands MUST be run in the production environment using strong, unique passwords.

**1.PostgreSQL Credentials:**
 ```bash
kubectl create secret generic postgres-secret -n nexus-ai \
  --from-literal=username='nexus' \
  --from-literal=password='<YOUR_STRONG_PROD_PASSWORD>'
  ```

**2.JWT Signing Key:**

```bash
kubectl create secret generic jwt-secret --from-literal=secret=$(openssl rand -base64 32) -n nexus-ai
```
**3.Deployment Command (After Secrets are Created):**
```bash
kubectl create namespace nexus-ai
kubectl apply -f k8s/
```

## Testing
Run all tests:
```bash
mvn clean test
```
Unit, integration (Testcontainers), and full E2E tests will be run automatically.

## API examples
- **User Registration:**
```bash
curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d '{"username": "e2e_user", "password": "Password123!", "email": "e2e@example.com"}'
```
- **JWT login:**
```bash
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"username": "e2e_user", "password": "Password123!"}'
```
- **AI Prediction:**
```bash
curl -X POST http://localhost:8080/api/analytics/predict -H "Authorization: Bearer <JWT_TOKEN>" -H "Content-Type: application/json" -d '{"input": [1.0,2.0,3.0,4.0,5.0]}'
```
- **Stripe Payments:**
```bash
curl -X POST http://localhost:8080/api/payments/charge -H "Authorization: Bearer <JWT_TOKEN>" -H "Content-Type: application/json" -d '{"amount": 99.99, "currency": "USD", "description": "Test Charge", "cardNumber": "4111...", "cardExpiry": "12/30", "cardCvc": "123"}'
```
- **Adding a block in Blockchain:**
```bash
curl -X POST http://localhost:8080/api/blockchain/add -H "Authorization: Bearer <JWT_TOKEN>" -H "Content-Type: application/json" -d '{"data": "My super secret E2E data"}'
```
- **WebSocket Notifications (JS):**
```JavaScript
stompClient.connect({}, function(frame) {
stompClient.subscribe('/topic/public', function(notification) {
console.log("Public:", JSON.parse(notification.body).content);
});
stompClient.subscribe('/user/queue/reply', function(notification) {
console.log("Private:", JSON.parse(notification.body).content);
});
stompClient.send("/app/broadcast", {}, JSON.stringify({'content': 'Message from customer!'}));
});
```