package com.edutech.edutechbackend.exception;


// RuntimeException = unchecked exception
// Spring can automatically handle these and return proper HTTP responses
// we don't need try-catch everywhere — Spring catches it globally
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String message) {
        super(message);
        // passes the message up to RuntimeException
        // we can read it later in global exception handler
        // e.g. "Email already registered: john@gmail.com"
    }
}
