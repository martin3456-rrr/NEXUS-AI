package org.example.nexusai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
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
                //user-service
                .route("user-service", r -> r.path("/api/users/**", "/api/auth/**")
                        .filters(f -> f.requestRateLimiter(c -> c.setKeyResolver(userKeyResolver())
                                .setRateLimiter(redisRateLimiter())
                                .setStatusCode(HttpStatus.TOO_MANY_REQUESTS))
                        )
                        .uri("lb://user-service"))

                //notification-service
                .route("notification-service", r -> r.path("/api/notifications/**")
                        .uri("lb://notification-service"))

                //payment-service
                .route("payment-service", r -> r.path("/api/payments/**")
                        .uri("lb://payment-service"))

                //ai-analytics-service
                .route("ai-analytics-service", r -> r.path("/api/analytics/**")
                        .filters(f -> f.requestRateLimiter(c -> c.setKeyResolver(userKeyResolver())
                                .setRateLimiter(redisRateLimiter())
                                .setStatusCode(HttpStatus.TOO_MANY_REQUESTS))
                        )
                        .uri("lb://ai-analytics-service"))

                .route("blockchain-service", r -> r.path("/api/blockchain/**").uri("lb://blockchain-service"))
                .route("voting-service", r -> r.path("/api/voting/**").uri("lb://voting-service"))
                .build();
    }

    //RedisRateLimiter
    @Bean
    public RateLimiter redisRateLimiter() {
        return new RedisRateLimiter(10, 20);
    }

    // Klucz resolver bazujący na "X-User-Id"
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> Mono.just(
                exchange.getRequest().getHeaders().getFirst("X-User-Id") != null
                        ? Objects.requireNonNull(exchange.getRequest().getHeaders().getFirst("X-User-Id"))
                        : "anonymous"
        );
    }

    //CORS
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
}
