package com.edutech.edutechbackend.exception;


import com.edutech.edutechbackend.dto.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
// ↑ combination of @ControllerAdvice + @ResponseBody
//   watches over ALL controllers in the application
//   intercepts exceptions before they reach the client
//   converts them to clean JSON responses
//
//   @ControllerAdvice → intercepts exceptions globally
//   @ResponseBody     → return values written as JSON
public class GlobalExceptionHandler {

    // ── Handler 1: Email Already Exists ──────────────────────────────────
    @ExceptionHandler(EmailAlreadyExistsException.class)
    // ↑ whenever EmailAlreadyExistsException is thrown ANYWHERE in the app
    //   Spring calls this method instead of crashing
    public ResponseEntity<ErrorResponseDTO> handleEmailAlreadyExists(
            EmailAlreadyExistsException ex) {

        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .status(HttpStatus.CONFLICT.value())
                // ↑ 409 Conflict
                //   means: resource already exists
                //   appropriate for duplicate email

                .message(ex.getMessage())
                // ↑ "Email already registered: john@gmail.com"
                //   the message we passed when throwing the exception

                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                // ↑ sets HTTP status code to 409
                .body(error);
        // ↑ sets response body to our ErrorResponseDTO as JSON
    }

    // ── Handler 2: Invalid Credentials ───────────────────────────────────
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidCredentials(
            InvalidCredentialsException ex) {

        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                // ↑ 401 Unauthorized
                //   means: authentication failed
                .message(ex.getMessage())
                // ↑ "Invalid email or password"
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(error);
    }

    // ── Handler 3: Validation Failures ───────────────────────────────────
    // triggered when @Valid fails on @RequestBody
    // e.g. empty email, short password, invalid email format
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        // ↑ will hold: { "email": "Enter a valid email address",
        //                "password": "Password must be at least 6 characters" }

        ex.getBindingResult()
                .getAllErrors()
                .forEach(error -> {
                    // cast to FieldError to get the field name
                    String fieldName = ((FieldError) error).getField();
                    // ↑ e.g. "email", "password", "name"

                    String errorMessage = error.getDefaultMessage();
                    // ↑ the message from our DTO annotation
                    //   e.g. "Enter a valid email address"

                    errors.put(fieldName, errorMessage);
                    // builds the map: "email" → "Enter a valid email address"
                });

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                // ↑ 400 Bad Request
                .body(errors);
        // ↑ returns map as JSON:
        // {
        //   "email": "Enter a valid email address",
        //   "password": "Password must be at least 6 characters"
        // }
    }

    // ── Handler 4: Generic fallback for any unhandled exception ──────────
    @ExceptionHandler(Exception.class)
    // ↑ catches ANY exception not handled by above handlers
    //   last resort safety net
    public ResponseEntity<ErrorResponseDTO> handleGenericException(
            Exception ex) {

        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                // ↑ 500 Internal Server Error
                .message("Something went wrong. Please try again.")
                // ↑ vague message intentionally
                //   don't expose internal error details to client
                //   log the actual error on server for debugging
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }

    // ── Handler 5: Student Not Found ─────────────────────────────────────
    @ExceptionHandler(StudentNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleStudentNotFound(
            StudentNotFoundException ex) {

        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .status(HttpStatus.NOT_FOUND.value())
                // ↑ 404 Not Found
                //   standard HTTP status when requested resource doesn't exist
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }
}
