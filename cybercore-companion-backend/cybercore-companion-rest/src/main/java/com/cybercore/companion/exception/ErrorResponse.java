package com.cybercore.companion.exception;

import lombok.Data;

@Data
public class ErrorResponse {
    private String message;
    private String details;

    public ErrorResponse(String message, String details) {
        this.message = message;
        this.details = details;
    }

    public ErrorResponse(String message) {
        this.message = message;
    }
}