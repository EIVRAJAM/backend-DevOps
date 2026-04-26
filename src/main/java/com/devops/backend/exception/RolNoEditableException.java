package com.devops.backend.exception;

public class RolNoEditableException extends RuntimeException{

    public RolNoEditableException(String message) {
        super(message);
    }

    public RolNoEditableException(String message, Throwable cause) {
        super(message, cause);
    }
}
