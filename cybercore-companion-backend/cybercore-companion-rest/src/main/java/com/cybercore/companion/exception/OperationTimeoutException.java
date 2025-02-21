package com.cybercore.companion.exception;

public class OperationTimeoutException extends RuntimeException {
    public OperationTimeoutException(String message) {
        super(message);
    }

    public OperationTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}