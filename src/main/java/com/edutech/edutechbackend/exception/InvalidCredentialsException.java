package com.edutech.edutechbackend.exception;


public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
        // e.g. "Invalid email or password"
    }
}