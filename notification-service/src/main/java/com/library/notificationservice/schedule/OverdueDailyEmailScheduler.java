package com.library.notificationservice.schedule;

import com.library.notificationservice.client.BorrowServiceClient;
import com.library.notificationservice.client.BorrowOverdueResponse;
import com.library.notificationservice.client.UserInfo;
import com.library.notificationservice.client.UserLookupClient;
import com.library.notificationservice.service.EmailNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "app.notification.scheduler.enabled", havingValue = "true")
public class OverdueDailyEmailScheduler {

    private static final Logger log = LoggerFactory.getLogger(OverdueDailyEmailScheduler.class);

    private final BorrowServiceClient borrowServiceClient;
    private final UserLookupClient userLookupClient;
    private final EmailNotificationService emailNotificationService;

    public OverdueDailyEmailScheduler(
            BorrowServiceClient borrowServiceClient,
            UserLookupClient userLookupClient,
            EmailNotificationService emailNotificationService) {
        this.borrowServiceClient = borrowServiceClient;
        this.userLookupClient = userLookupClient;
        this.emailNotificationService = emailNotificationService;
    }

    @Scheduled(cron = "${app.notification.scheduler.cron}", zone = "${app.notification.scheduler.zone}")
    public void sendDailyOverdueEmails() {
        List<BorrowOverdueResponse> overdue = borrowServiceClient.getOverdueBorrows();
        if (overdue.isEmpty()) {
            log.debug("No overdue borrows found today.");
            return;
        }

        Map<Long, List<BorrowOverdueResponse>> byUser =
                overdue.stream().collect(Collectors.groupingBy(BorrowOverdueResponse::userId));

        int emailsAttempted = 0;
        for (Map.Entry<Long, List<BorrowOverdueResponse>> entry : byUser.entrySet()) {
            long userId = entry.getKey();
            List<BorrowOverdueResponse> items = entry.getValue();

            Optional<UserInfo> userOpt = userLookupClient.findById(userId);
            if (userOpt.isEmpty() || userOpt.get().email() == null || userOpt.get().email().isBlank()) {
                log.warn("Skipping overdue email for userId={} (missing user or email)", userId);
                continue;
            }

            try {
                emailNotificationService.sendDailyOverdueReminder(userOpt.get().email(), items);
                emailsAttempted++;
            } catch (Exception ex) {
                log.error("Failed to send daily overdue email for userId={}: {}", userId, ex.getMessage(), ex);
            }
        }

        log.info("Daily overdue email job finished. Attempted emails={}", emailsAttempted);
    }
}

