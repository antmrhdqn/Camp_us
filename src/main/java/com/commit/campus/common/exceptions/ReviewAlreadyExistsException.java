package com.commit.campus.common.exceptions;

import org.springframework.http.HttpStatus;

public class ReviewAlreadyExistsException extends RuntimeException{

    private final HttpStatus status;

    public ReviewAlreadyExistsException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

}
