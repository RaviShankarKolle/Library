package com.library.bookservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.bookservice.service.LibraryBookService;
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
public class BookKafkaListeners {

    private static final Logger log = LoggerFactory.getLogger(BookKafkaListeners.class);

    private final LibraryBookService libraryBookService;
    private final ObjectMapper objectMapper;

    public BookKafkaListeners(LibraryBookService libraryBookService, ObjectMapper objectMapper) {
        this.libraryBookService = libraryBookService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.kafka.topic-book-issued}", groupId = "${spring.kafka.consumer.group-id}")
    public void onBookIssued(@Payload String json, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        BookLendingPayload p = read(json, topic);
        if (p == null || p.copyId() == null) {
            log.warn("BOOK_ISSUED invalid payload from {}", topic);
            return;
        }
        log.debug("BOOK_ISSUED copyId={}", p.copyId());
        libraryBookService.applyBookIssued(p.copyId());
    }

    @KafkaListener(topics = "${app.kafka.topic-book-returned}", groupId = "${spring.kafka.consumer.group-id}")
    public void onBookReturned(@Payload String json, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        BookLendingPayload p = read(json, topic);
        if (p == null || p.copyId() == null) {
            log.warn("BOOK_RETURNED invalid payload from {}", topic);
            return;
        }
        log.debug("BOOK_RETURNED copyId={}", p.copyId());
        libraryBookService.applyBookReturned(p.copyId());
    }

    private BookLendingPayload read(String json, String topic) {
        try {
            return objectMapper.readValue(json, BookLendingPayload.class);
        } catch (JsonProcessingException e) {
            log.warn("Invalid JSON from {}: {}", topic, e.getMessage());
            return null;
        }
    }
}
