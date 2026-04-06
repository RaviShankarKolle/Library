package com.library.fineservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.fineservice.service.LibraryFineService;
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
public class OverdueListener {

    private static final Logger log = LoggerFactory.getLogger(OverdueListener.class);

    private final LibraryFineService libraryFineService;
    private final ObjectMapper objectMapper;

    public OverdueListener(LibraryFineService libraryFineService, ObjectMapper objectMapper) {
        this.libraryFineService = libraryFineService;
        this.objectMapper = objectMapper;
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
        log.debug("OVERDUE lendingId={} userId={}", payload.lendingId(), payload.userId());
        libraryFineService.accrueFromOverdueEventUtcToday(payload);
    }
}
