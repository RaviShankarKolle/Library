package com.library.borrowservice.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientConfiguration {

    @Bean
    @Qualifier("userServiceClient")
    RestClient userServiceClient(@LoadBalanced RestClient.Builder builder) {
        // Uses Eureka discovery via load balancer
        return builder.baseUrl("http://user-service").build();
    }

    @Bean
    @Qualifier("bookServiceClient")
    RestClient bookServiceClient(@LoadBalanced RestClient.Builder builder) {
        // Uses Eureka discovery via load balancer
        return builder.baseUrl("http://book-service").build();
    }
}
