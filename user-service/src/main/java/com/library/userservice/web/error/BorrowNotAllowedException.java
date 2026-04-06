package com.library.userservice.web.error;

public class BorrowNotAllowedException extends RuntimeException {

    public BorrowNotAllowedException(String message) {
        super(message);
    }
}
