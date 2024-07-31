package com.commit.campus.common.exceptions;

import org.springframework.http.HttpStatus;

public class ReviewNotFoundException extends RuntimeException {

    private final HttpStatus status;

    public ReviewNotFoundException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
