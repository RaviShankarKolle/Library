package com.library.userservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.userservice.service.LibraryUserService;
import com.library.userservice.web.error.BorrowNotAllowedException;
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
public class UserKafkaListeners {

    private static final Logger log = LoggerFactory.getLogger(UserKafkaListeners.class);

    private final LibraryUserService libraryUserService;
    private final ObjectMapper objectMapper;

    public UserKafkaListeners(LibraryUserService libraryUserService, ObjectMapper objectMapper) {
        this.libraryUserService = libraryUserService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.kafka.topic-book-issued}", groupId = "${spring.kafka.consumer.group-id}")
    public void onBookIssued(@Payload String json, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        BookIssuedPayload payload = read(json, BookIssuedPayload.class, topic);
        if (payload == null || payload.userId() == null) {
            log.warn("Ignoring BOOK_ISSUED from {}: missing or invalid userId", topic);
            return;
        }
        log.debug("BOOK_ISSUED userId={} lendingId={}", payload.userId(), payload.lendingId());
        try {
            libraryUserService.incrementBorrowCount(payload.userId());
        } catch (BorrowNotAllowedException ex) {
            log.warn("BOOK_ISSUED not applied: {}", ex.getMessage());
        }
    }

    @KafkaListener(topics = "${app.kafka.topic-book-returned}", groupId = "${spring.kafka.consumer.group-id}")
    public void onBookReturned(@Payload String json, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        BookReturnedPayload payload = read(json, BookReturnedPayload.class, topic);
        if (payload == null || payload.userId() == null) {
            log.warn("Ignoring BOOK_RETURNED from {}: missing or invalid userId", topic);
            return;
        }
        log.debug("BOOK_RETURNED userId={} lendingId={}", payload.userId(), payload.lendingId());
        libraryUserService.decrementBorrowCount(payload.userId());
    }

    private <T> T read(String json, Class<T> type, String topic) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            log.warn("Invalid JSON from {}: {}", topic, e.getMessage());
            return null;
        }
    }
}
