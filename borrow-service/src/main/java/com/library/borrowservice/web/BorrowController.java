package com.library.borrowservice.web;

import com.library.borrowservice.domain.BorrowRecord;
import com.library.borrowservice.service.LibraryBorrowService;
import com.library.borrowservice.web.dto.BorrowResponse;
import com.library.borrowservice.web.dto.CreateBorrowRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/borrows")
public class BorrowController {

    private final LibraryBorrowService libraryBorrowService;

    public BorrowController(LibraryBorrowService libraryBorrowService) {
        this.libraryBorrowService = libraryBorrowService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BorrowResponse create(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @Valid @RequestBody CreateBorrowRequest body) {
        BorrowRecord r = libraryBorrowService.requestBorrow(
                body.userId(), body.barcode(), body.resolvedLoanDays(), authorization);
        return BorrowResponse.from(r);
    }

    @PostMapping("/{id}/issue")
    public BorrowResponse issue(@PathVariable long id) {
        return BorrowResponse.from(libraryBorrowService.issueBook(id));
    }

    @PostMapping("/{id}/return")
    public BorrowResponse returnBook(@PathVariable long id) {
        return BorrowResponse.from(libraryBorrowService.returnBook(id));
    }

    @GetMapping("/overdue")
    public List<BorrowResponse> overdue() {
        return libraryBorrowService.getOverdue().stream().map(BorrowResponse::from).toList();
    }
}
