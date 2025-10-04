package org.example.nexusai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Mono;

import java.util.Objects;

@SpringBootApplication
@EnableDiscoveryClient
public class NexusAiApplication {
    public static void main(String[] args) {
        SpringApplication.run(NexusAiApplication.class, args);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Routing do user-service
                .route("user-service", r -> r.path("/api/users/**", "/api/auth/**")
                        .uri("lb://user-service"))

                // Routing do notification-service
                .route("notification-service", r -> r.path("/api/notifications/**")
                        .uri("lb://notification-service"))

                // Routing do payment-service
                .route("payment-service", r -> r.path("/api/payments/**")
                        .uri("lb://payment-service"))

                // Routing do ai-analytics-service
                .route("ai-analytics-service", r -> r.path("/api/analytics/**")
                        .uri("lb://ai-analytics-service"))

                // Routing do blockchain-service
                .route("blockchain-service", r -> r.path("/api/blockchain/**")
                        .uri("lb://blockchain-service"))

                // Routing do voting-service
                .route("voting-service", r -> r.path("/api/voting/**")
                        .uri("lb://voting-service"))

                .build();
    }
    @Bean
    public KeyResolver userKeyResolver() {
        // Rate limit based on the client's IP address
        return exchange -> Mono.just(Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress());
    }
}
