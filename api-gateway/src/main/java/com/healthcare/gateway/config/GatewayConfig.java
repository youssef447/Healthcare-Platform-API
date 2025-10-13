package com.healthcare.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Data Ingestion Service Routes
                .route("data-ingestion", r ->
                        r.path("/api/ingestion/**")
                                .uri("lb://data-ingestion-service"))// lb from eureka

                // Patient Management Service Routes
                .route("patient-management", r ->
                        r.path("/api/patients/**", "/api/treatments/**", "/api/appointments/**")
                                .uri("lb://patient-management-service"))

                // Medical Dashboard Service Routes
                .route("medical-dashboard", r ->
                        r.path("/api/dashboard/**", "/api/reports/**")
                                .uri("lb://medical-dashboard-service"))

                /*   // Eureka Server Route
                   .route("eureka-server", r -> r.path("/eureka/**")
                           .uri("lb://eureka-server"))*/

                .build();
    }
}
