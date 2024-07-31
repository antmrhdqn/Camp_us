package com.commit.campus.common.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ReviewAlreadyExistsException.class)
    public ResponseEntity<String> handleReviewAlreadyExists(ReviewAlreadyExistsException ex) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(ex.getMessage());
    }

    @ExceptionHandler(ReviewNotFoundException.class)
    public ResponseEntity<String> handleReviewNotFoundException(ReviewNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }
}
