package com.library.bookservice.web.dto;

import com.library.bookservice.domain.BookCopy;

public record CopyResponse(long id, long bookId, String barcode, String status) {

    public static CopyResponse from(BookCopy c) {
        return new CopyResponse(c.getId(), c.getBookId(), c.getBarcode(), c.getStatus());
    }
}
