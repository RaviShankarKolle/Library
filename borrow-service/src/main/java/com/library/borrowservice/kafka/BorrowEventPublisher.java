package com.library.borrowservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.borrowservice.domain.BorrowRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class BorrowEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(BorrowEventPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final boolean kafkaEnabled;
    private final String topicBookIssued;
    private final String topicBookReturned;
    private final String topicOverdue;

    public BorrowEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${app.kafka.enabled:false}") boolean kafkaEnabled,
            @Value("${app.kafka.topic-book-issued}") String topicBookIssued,
            @Value("${app.kafka.topic-book-returned}") String topicBookReturned,
            @Value("${app.kafka.topic-overdue}") String topicOverdue) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.kafkaEnabled = kafkaEnabled;
        this.topicBookIssued = topicBookIssued;
        this.topicBookReturned = topicBookReturned;
        this.topicOverdue = topicOverdue;
    }

    public void publishBookIssued(BorrowRecord r) {
        if (!kafkaEnabled) {
            log.debug("Kafka disabled, skip BOOK_ISSUED");
            return;
        }
        send(topicBookIssued, r.getId(), new BookLendingPayload(r.getUserId(), r.getId(), r.getCopyId(), r.getBarcode()));
    }

    public void publishBookReturned(BorrowRecord r) {
        if (!kafkaEnabled) {
            log.debug("Kafka disabled, skip BOOK_RETURNED");
            return;
        }
        send(topicBookReturned, r.getId(), new BookLendingPayload(r.getUserId(), r.getId(), r.getCopyId(), r.getBarcode()));
    }

    public void publishOverdue(BorrowRecord r) {
        if (!kafkaEnabled) {
            return;
        }
        if (r.getDueDate() == null) {
            return;
        }
        OverduePayload payload = new OverduePayload(
                r.getUserId(),
                r.getId(),
                r.getCopyId(),
                r.getBarcode(),
                r.getDueDate().toString());
        send(topicOverdue, r.getId(), payload);
    }

    private void send(String topic, long key, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(topic, String.valueOf(key), json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Kafka payload", e);
        }
    }
}
