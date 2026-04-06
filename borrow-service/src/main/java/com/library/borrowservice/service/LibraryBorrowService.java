package com.library.borrowservice.service;

import com.library.borrowservice.client.BookServiceClient;
import com.library.borrowservice.client.CopyInfo;
import com.library.borrowservice.client.UserServiceClient;
import com.library.borrowservice.domain.BorrowRecord;
import com.library.borrowservice.kafka.BorrowEventPublisher;
import com.library.borrowservice.repository.BorrowRepository;
import com.library.borrowservice.web.error.ConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class LibraryBorrowService {

    private static final String AVAILABLE = "AVAILABLE";

    private final BorrowRepository borrowRepository;
    private final UserServiceClient userServiceClient;
    private final BookServiceClient bookServiceClient;
    private final BorrowEventPublisher borrowEventPublisher;

    public LibraryBorrowService(
            BorrowRepository borrowRepository,
            UserServiceClient userServiceClient,
            BookServiceClient bookServiceClient,
            BorrowEventPublisher borrowEventPublisher) {
        this.borrowRepository = borrowRepository;
        this.userServiceClient = userServiceClient;
        this.bookServiceClient = bookServiceClient;
        this.borrowEventPublisher = borrowEventPublisher;
    }

    @Transactional
    public BorrowRecord requestBorrow(long userId, String barcode, int loanDays, String authorizationHeader) {
        userServiceClient.validateBorrowEligibility(userId, authorizationHeader);
        CopyInfo copy = bookServiceClient.getCopyByBarcode(barcode, authorizationHeader);
        if (!AVAILABLE.equals(copy.status())) {
            throw new ConflictException("Copy not available, status=" + copy.status());
        }
        bookServiceClient.reserve(barcode, authorizationHeader);
        try {
            return borrowRepository.insertReserved(userId, copy.id(), barcode, loanDays);
        } catch (RuntimeException e) {
            bookServiceClient.releaseReservation(barcode, authorizationHeader);
            throw e;
        }
    }

    @Transactional
    public BorrowRecord issueBook(long borrowId) {
        BorrowRecord record = borrowRepository.findById(borrowId).orElseThrow(() -> new ConflictException("Borrow not found"));
        if (!BorrowRecord.RESERVED.equals(record.getStatus())) {
            throw new ConflictException("Borrow must be RESERVED to issue");
        }
        Instant borrowDate = Instant.now();
        Instant dueDate = borrowDate.plus(record.getLoanDays(), ChronoUnit.DAYS);
        int updated = borrowRepository.markAllocated(borrowId, borrowDate, dueDate);
        if (updated == 0) {
            throw new ConflictException("Could not allocate borrow");
        }
        BorrowRecord after = borrowRepository.findById(borrowId).orElseThrow();
        borrowEventPublisher.publishBookIssued(after);
        return after;
    }

    @Transactional
    public BorrowRecord returnBook(long borrowId) {
        BorrowRecord record = borrowRepository.findById(borrowId).orElseThrow(() -> new ConflictException("Borrow not found"));
        if (!BorrowRecord.ALLOCATED.equals(record.getStatus())) {
            throw new ConflictException("Borrow must be ALLOCATED to return");
        }
        Instant returnedAt = Instant.now();
        int updated = borrowRepository.markReturned(borrowId, returnedAt);
        if (updated == 0) {
            throw new ConflictException("Could not return borrow");
        }
        BorrowRecord after = borrowRepository.findById(borrowId).orElseThrow();
        borrowEventPublisher.publishBookReturned(after);
        return after;
    }

    public java.util.List<BorrowRecord> getOverdue() {
        return borrowRepository.findOverdue(Instant.now());
    }
}
