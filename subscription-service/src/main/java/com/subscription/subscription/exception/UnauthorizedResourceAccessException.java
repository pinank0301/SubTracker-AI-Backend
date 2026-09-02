package com.subscription.subscription.exception;

public class UnauthorizedResourceAccessException extends RuntimeException {

    public UnauthorizedResourceAccessException(String message) {
        super(message);
    }

    public UnauthorizedResourceAccessException() {
        super("You are not authorized to access this resource");
    }
}
