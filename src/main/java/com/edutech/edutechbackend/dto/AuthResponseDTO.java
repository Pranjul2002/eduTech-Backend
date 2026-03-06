package com.edutech.edutechbackend.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@Builder
public class AuthResponseDTO {

    private String token;        // the JWT token student will use for future requests
    private String name;
    private String email;
    private String message;      // e.g. "Registration successful"
}
