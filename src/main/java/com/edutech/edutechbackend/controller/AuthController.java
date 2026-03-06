package com.edutech.edutechbackend.controller;


import com.edutech.edutechbackend.dto.AuthResponseDTO;
import com.edutech.edutechbackend.dto.LoginRequestDTO;
import com.edutech.edutechbackend.dto.RegisterRequestDTO;
import com.edutech.edutechbackend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
// ↑ marks this as a REST controller
//   all methods return JSON automatically
//   no view templates involved

@RequestMapping("/api/auth")
// ↑ base URL for all methods in this controller
//   every method URL = /api/auth + method mapping

@RequiredArgsConstructor
// ↑ constructor injection for AuthService
public class AuthController {

    private final AuthService authService;
    // ↑ the brain — all logic lives there
    //   controller just receives, delegates, returns
    //   thin controller = good practice

    // ════════════════════════════════════════════════════════════
    // REGISTER ENDPOINT
    // POST /api/auth/register
    // ════════════════════════════════════════════════════════════
    @PostMapping("/register")
    // ↑ maps HTTP POST /api/auth/register to this method
    //   POST because we're CREATING a new student resource
    public ResponseEntity<AuthResponseDTO> register(
            @Valid @RequestBody RegisterRequestDTO request) {
        //  ↑      ↑
        //  │      └── reads JSON body and converts to RegisterRequestDTO
        //  │          {
        //  │            "name": "John",
        //  │            "email": "john@gmail.com",
        //  │            "password": "mypassword123"
        //  │          }
        //  │
        //  └── triggers validation BEFORE this method runs:
        //        @NotBlank on name     → fails if empty
        //        @Email on email       → fails if invalid format
        //        @NotBlank on email    → fails if empty
        //        @Size(min=6) password → fails if less than 6 chars
        //        if any fail → MethodArgumentNotValidException thrown
        //                    → GlobalExceptionHandler catches it
        //                    → 400 returned, this method never runs

        AuthResponseDTO response = authService.register(request);
        // ↑ delegates ALL business logic to AuthService:
        //     → check email duplicate
        //     → encode password
        //     → save to DB
        //     → generate JWT token
        //     → return AuthResponseDTO

        return ResponseEntity
                .status(HttpStatus.CREATED)
                // ↑ 201 Created
                //   industry standard for successful resource creation
                //   tells client: a new student was created in the DB
                .body(response);
        // ↑ AuthResponseDTO converted to JSON:
        // {
        //   "token": "eyJhbGciOiJIUzI1NiJ9...",
        //   "name": "John",
        //   "email": "john@gmail.com",
        //   "message": "Registration successful"
        // }
    }

    // ════════════════════════════════════════════════════════════
    // LOGIN ENDPOINT
    // POST /api/auth/login
    // ════════════════════════════════════════════════════════════
    @PostMapping("/login")
    // ↑ maps HTTP POST /api/auth/login to this method
    //   POST because we're CREATING a new session/token
    //   (even though it feels like a read operation)
    //   also: GET requests should not have sensitive data in body
    //         POST keeps credentials safe in request body
    public ResponseEntity<AuthResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request) {
        //  ↑      ↑
        //  │      └── reads JSON body:
        //  │          {
        //  │            "email": "john@gmail.com",
        //  │            "password": "mypassword123"
        //  │          }
        //  │
        //  └── validates:
        //        @NotBlank + @Email on email
        //        @NotBlank on password
        //        fails → 400 Bad Request

        AuthResponseDTO response = authService.login(request);
        // ↑ delegates to AuthService:
        //     → authManager.authenticate() → verify credentials
        //     → find student from DB
        //     → generate JWT token
        //     → return AuthResponseDTO
        //
        // if wrong credentials → InvalidCredentialsException thrown
        //                      → GlobalExceptionHandler catches it
        //                      → 401 returned, nothing below runs

        return ResponseEntity
                .ok(response);
        // ↑ 200 OK
        //   login doesn't CREATE a resource, it RETRIEVES a token
        //   so 200 is appropriate (not 201)
        //
        // Response JSON:
        // {
        //   "token": "eyJhbGciOiJIUzI1NiJ9...",
        //   "name": "John",
        //   "email": "john@gmail.com",
        //   "message": "Login successful"
        // }
    }
}