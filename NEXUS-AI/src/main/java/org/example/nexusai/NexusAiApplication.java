package org.example.nexusai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
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
                // Routing do user-service z rate limitingiem
                .route("user-service", r -> r.path("/api/users/**", "/api/auth/**")
                        .filters(f -> f.requestRateLimiter(c -> c.setKeyResolver(userKeyResolver())
                                .setRateLimiter(redisRateLimiter())
                                .setStatusCode(HttpStatus.TOO_MANY_REQUESTS))
                        )
                        .uri("lb://user-service"))

                // Routing do notification-service
                .route("notification-service", r -> r.path("/api/notifications/**")
                        .uri("lb://notification-service"))

                // Routing do payment-service
                .route("payment-service", r -> r.path("/api/payments/**")
                        .uri("lb://payment-service"))

                // Routing do ai-analytics-service z rate limitingiem
                .route("ai-analytics-service", r -> r.path("/api/analytics/**")
                        .filters(f -> f.requestRateLimiter(c -> c.setKeyResolver(userKeyResolver())
                                .setRateLimiter(redisRateLimiter())
                                .setStatusCode(HttpStatus.TOO_MANY_REQUESTS))
                        )
                        .uri("lb://ai-analytics-service"))

                // Routing do blockchain-service
                .route("blockchain-service", r -> r.path("/api/blockchain/**")
                        .uri("lb://blockchain-service"))

                // Routing do voting-service
                .route("voting-service", r -> r.path("/api/voting/**")
                        .uri("lb://voting-service"))
                .build();
    }

    // Konfiguracja rate limiter: bean RedisRateLimiter lub inny mechanizm, przykład fikcyjny poniżej
    @Bean
    public RateLimiter redisRateLimiter() {
        return new GridBucketState(10, 10, Duration.ofSeconds(1));
    }

    // Bean KeyResolver — rozwiązanie klucza ograniczenia na użytkownika
    @Bean
    public org.springframework.cloud.gateway.filter.ratelimit.KeyResolver userKeyResolver() {
        return exchange -> Mono.just(
                exchange.getRequest().getHeaders().getFirst("X-User-Id") != null
                        ? Objects.requireNonNull(exchange.getRequest().getHeaders().getFirst("X-User-Id"))
                        : "anonymous"
        );
    }

    // CORS konfiguracja
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(List.of("http://localhost:3000", "http://127.0.0.1:3000"));
        corsConfig.setMaxAge(3600L);
        corsConfig.addAllowedMethod("*");
        corsConfig.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return new CorsWebFilter(source);
    }

    private static class GridBucketState implements RateLimiter {
        public GridBucketState(int i, int i1, Duration duration) {
        }

        @Override
        public Mono<Response> isAllowed(String routeId, String id) {
            return null;
        }

        @Override
        public Map getConfig() {
            return Map.of();
        }

        @Override
        public Class getConfigClass() {
            return null;
        }

        @Override
        public Object newConfig() {
            return null;
        }
    }
}
