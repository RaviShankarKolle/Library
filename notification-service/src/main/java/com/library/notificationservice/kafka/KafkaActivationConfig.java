package com.library.notificationservice.kafka;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
@EnableKafka
public class KafkaActivationConfig {
}
