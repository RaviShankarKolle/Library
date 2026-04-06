package com.library.notificationservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.notificationservice.client.UserInfo;
import com.library.notificationservice.client.UserLookupClient;
import com.library.notificationservice.service.EmailNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class OverdueNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(OverdueNotificationListener.class);

    private final ObjectMapper objectMapper;
    private final UserLookupClient userLookupClient;
    private final EmailNotificationService emailNotificationService;

    public OverdueNotificationListener(
            ObjectMapper objectMapper,
            UserLookupClient userLookupClient,
            EmailNotificationService emailNotificationService) {
        this.objectMapper = objectMapper;
        this.userLookupClient = userLookupClient;
        this.emailNotificationService = emailNotificationService;
    }

    @KafkaListener(topics = "${app.kafka.topic-overdue}", groupId = "${spring.kafka.consumer.group-id}")
    public void onOverdue(@Payload String json, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        OverduePayload payload;
        try {
            payload = objectMapper.readValue(json, OverduePayload.class);
        } catch (JsonProcessingException e) {
            log.warn("Invalid overdue JSON from {}: {}", topic, e.getMessage());
            return;
        }
        if (payload.userId() == null) {
            log.warn("OVERDUE payload missing userId, skipping email");
            return;
        }

        UserInfo user = userLookupClient.findById(payload.userId()).orElse(null);
        if (user == null || user.email() == null || user.email().isBlank()) {
            log.warn("Cannot send overdue email, user/email not found for userId={}", payload.userId());
            return;
        }

        try {
            emailNotificationService.sendOverdueReminder(user.email(), payload);
            log.info("Overdue reminder sent to {} for lendingId={}", user.email(), payload.lendingId());
        } catch (Exception ex) {
            log.error("Failed to send overdue email to {}: {}", user.email(), ex.getMessage(), ex);
        }
    }
}
