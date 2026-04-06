package com.library.bookservice.web.dto;

import com.library.bookservice.domain.Book;

public record BookResponse(long id, String isbn, String title, String author) {

    public static BookResponse from(Book b) {
        return new BookResponse(b.getId(), b.getIsbn(), b.getTitle(), b.getAuthor());
    }
}
