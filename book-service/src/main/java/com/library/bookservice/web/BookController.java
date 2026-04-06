package com.library.bookservice.web;

import com.library.bookservice.service.LibraryBookService;
import com.library.bookservice.web.dto.AddCopyRequest;
import com.library.bookservice.web.dto.BookResponse;
import com.library.bookservice.web.dto.CopyResponse;
import com.library.bookservice.web.dto.CreateBookRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class BookController {

    private final LibraryBookService libraryBookService;

    public BookController(LibraryBookService libraryBookService) {
        this.libraryBookService = libraryBookService;
    }

    @PostMapping("/books")
    @ResponseStatus(HttpStatus.CREATED)
    public BookResponse createBook(@Valid @RequestBody CreateBookRequest body) {
        return BookResponse.from(
                libraryBookService.createBook(body.isbn(), body.title(), body.author()));
    }

    @PostMapping("/books/{bookId}/copies")
    @ResponseStatus(HttpStatus.CREATED)
    public CopyResponse addCopy(@PathVariable long bookId, @Valid @RequestBody AddCopyRequest body) {
        return CopyResponse.from(libraryBookService.addCopy(bookId, body.barcode()));
    }

    @GetMapping("/copies/barcode/{barcode}")
    public CopyResponse getByBarcode(@PathVariable String barcode) {
        return CopyResponse.from(libraryBookService.getCopyByBarcode(barcode));
    }

    @PostMapping("/copies/barcode/{barcode}/reserve")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reserve(@PathVariable String barcode) {
        libraryBookService.reserveCopy(barcode);
    }

    /** Compensating action if borrow persistence fails after a reserve. */
    @PostMapping("/copies/barcode/{barcode}/release-reservation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void releaseReservation(@PathVariable String barcode) {
        libraryBookService.releaseReservation(barcode);
    }
}
