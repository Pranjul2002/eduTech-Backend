package com.edutech.edutechbackend.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter @Setter
@AllArgsConstructor
@Builder
public class ErrorResponseDTO {

    private int status;
    // ↑ HTTP status code
    //   e.g. 400, 401, 404, 409, 500

    private String message;
    // ↑ human readable error description
    //   e.g. "Email already registered: john@gmail.com"

    private LocalDateTime timestamp;
    // ↑ when the error occurred
    //   useful for debugging and logging
}
