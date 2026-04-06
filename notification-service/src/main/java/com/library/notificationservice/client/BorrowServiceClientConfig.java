package com.library.notificationservice.client;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class BorrowServiceClientConfig {

    @Bean
    @LoadBalanced
    RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    RestClient borrowServiceClient(RestClient.Builder builder) {
        // Uses Eureka discovery via load balancer
        return builder.baseUrl("http://borrow-service").build();
    }
}

