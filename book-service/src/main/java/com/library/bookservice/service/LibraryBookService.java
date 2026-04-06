package com.library.bookservice.service;

import com.library.bookservice.domain.Book;
import com.library.bookservice.domain.BookCopy;
import com.library.bookservice.repository.BookRepository;
import com.library.bookservice.web.error.ConflictException;
import com.library.bookservice.web.error.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LibraryBookService {

    private final BookRepository bookRepository;

    public LibraryBookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Book createBook(String isbn, String title, String author) {
        return bookRepository.insertBook(isbn, title, author);
    }

    public BookCopy addCopy(long bookId, String barcode) {
        bookRepository.findBookById(bookId).orElseThrow(() -> new NotFoundException("Book not found"));
        try {
            return bookRepository.insertCopy(bookId, barcode, BookCopy.AVAILABLE);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            throw new ConflictException("Barcode already exists");
        }
    }

    public BookCopy getCopyByBarcode(String barcode) {
        return bookRepository.findCopyByBarcode(barcode).orElseThrow(() -> new NotFoundException("Copy not found"));
    }

    public void checkAvailable(String barcode) {
        BookCopy c = getCopyByBarcode(barcode);
        if (!BookCopy.AVAILABLE.equals(c.getStatus())) {
            throw new ConflictException("Copy not available: " + c.getStatus());
        }
    }

    @Transactional
    public void reserveCopy(String barcode) {
        checkAvailable(barcode);
        int n = bookRepository.reserveIfAvailable(barcode);
        if (n == 0) {
            throw new ConflictException("Could not reserve copy (race or status)");
        }
    }

    @Transactional
    public void releaseReservation(String barcode) {
        bookRepository.releaseReservation(barcode);
    }

    @Transactional
    public void applyBookIssued(long copyId) {
        int n = bookRepository.markIssued(copyId);
        if (n == 0) {
            // idempotent: already ISSUED
            BookCopy c = bookRepository.findCopyById(copyId).orElse(null);
            if (c == null || !BookCopy.ISSUED.equals(c.getStatus())) {
                throw new ConflictException("Cannot mark issued for copy " + copyId);
            }
        }
    }

    @Transactional
    public void applyBookReturned(long copyId) {
        int n = bookRepository.markReturned(copyId);
        if (n == 0) {
            BookCopy c = bookRepository.findCopyById(copyId).orElse(null);
            if (c == null || !BookCopy.AVAILABLE.equals(c.getStatus())) {
                throw new ConflictException("Cannot mark returned for copy " + copyId);
            }
        }
    }
}
