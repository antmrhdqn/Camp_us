package com.commit.campus.common.exceptions;

import com.commit.campus.view.ErrorView;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ReviewAlreadyExistsException.class)
    public ResponseEntity<ErrorView> handleReviewAlreadyExists(ReviewAlreadyExistsException ex) {

        ErrorView errorView = new ErrorView("REVIEW_ALREADY_EXISTS", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorView);
    }

    @ExceptionHandler(NotAuthorizedException.class)
    public ResponseEntity<ErrorView> handleNotAuthorizedException(NotAuthorizedException ex) {

        ErrorView errorView = new ErrorView("NOT_AUTHORIZED", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(errorView);
    }
}
