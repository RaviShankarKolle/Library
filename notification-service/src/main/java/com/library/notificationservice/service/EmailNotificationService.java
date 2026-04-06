package com.library.notificationservice.service;

import com.library.notificationservice.client.BorrowOverdueResponse;
import com.library.notificationservice.kafka.OverduePayload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailNotificationService {

    private final JavaMailSender mailSender;
    private final String fromEmail;

    public EmailNotificationService(
            JavaMailSender mailSender, @Value("${app.notification.from-email}") String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    public void sendOverdueReminder(String toEmail, OverduePayload payload) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromEmail);
        msg.setTo(toEmail);
        msg.setSubject("Library overdue reminder");
        msg.setText(buildBody(payload));
        mailSender.send(msg);
    }

    public void sendDailyOverdueReminder(String toEmail, List<BorrowOverdueResponse> overdueBorrows) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromEmail);
        msg.setTo(toEmail);
        msg.setSubject("Your library overdue reminder (daily)");
        msg.setText(buildDailyBody(overdueBorrows));
        mailSender.send(msg);
    }

    private String buildBody(OverduePayload payload) {
        return "Your borrowed book is overdue.\n\n"
                + "Borrow ID: " + payload.lendingId() + "\n"
                + "Copy barcode: " + payload.barcode() + "\n"
                + "Due date: " + payload.dueDate() + "\n\n"
                + "Please return the book as soon as possible to avoid additional fines.\n";
    }

    private String buildDailyBody(List<BorrowOverdueResponse> items) {
        if (items == null || items.isEmpty()) {
            return "You have no overdue books right now.\n";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Good day,\n\n");
        sb.append("Here are your overdue borrow items:\n\n");
        for (BorrowOverdueResponse i : items) {
            sb.append("- Borrow ID: ").append(i.id()).append(", ")
                    .append("Barcode: ").append(i.barcode()).append(", ")
                    .append("Due date: ").append(i.dueDate()).append("\n");
        }
        sb.append("\nPlease return the books as soon as possible to avoid additional fines.\n");
        return sb.toString();
    }
}
