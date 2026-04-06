package com.library.borrowservice.schedule;

import com.library.borrowservice.domain.BorrowRecord;
import com.library.borrowservice.kafka.BorrowEventPublisher;
import com.library.borrowservice.repository.BorrowRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class OverdueScheduler {

    private static final Logger log = LoggerFactory.getLogger(OverdueScheduler.class);

    private final BorrowRepository borrowRepository;
    private final BorrowEventPublisher borrowEventPublisher;
    private final boolean kafkaEnabled;

    public OverdueScheduler(
            BorrowRepository borrowRepository,
            BorrowEventPublisher borrowEventPublisher,
            @Value("${app.kafka.enabled:false}") boolean kafkaEnabled) {
        this.borrowRepository = borrowRepository;
        this.borrowEventPublisher = borrowEventPublisher;
        this.kafkaEnabled = kafkaEnabled;
    }

    @Scheduled(cron = "${app.borrow.overdue-cron}", zone = "${app.borrow.overdue-zone}")
    public void publishOverdueEvents() {
        if (!kafkaEnabled) {
            return;
        }
        Instant now = Instant.now();
        List<BorrowRecord> overdue = borrowRepository.findOverdue(now);
        if (!overdue.isEmpty()) {
            log.info("Publishing {} overdue event(s)", overdue.size());
        }
        for (BorrowRecord b : overdue) {
            borrowEventPublisher.publishOverdue(b);
        }
    }
}
