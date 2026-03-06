package com.edutech.edutechbackend.exception;


public class StudentNotFoundException extends RuntimeException {

    public StudentNotFoundException(String message) {
        super(message);
        // e.g. "Student not found with email: john@gmail.com"
    }
}
