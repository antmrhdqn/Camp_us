package com.commit.campus.view;

import lombok.Data;

@Data
public class ErrorView {
    private String errorCode;
    private String message;

    public ErrorView(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }
}