package com.edutech.edutechbackend.dto;


import com.edutech.edutechbackend.entity.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter
@AllArgsConstructor
@Builder
public class StudentProfileDTO {

    private Long id;
    private String name;
    private String email;

    private Gender gender;
    // ↑ "MALE", "FEMALE", or "OTHER" in JSON response

    private LocalDate dateOfBirth;
    // ↑ "2000-05-15" in JSON response

    private int age;
    // ↑ calculated dynamically from dateOfBirth
    //   always accurate, never stale
    //   e.g. 23

    private LocalDateTime createdAt;   // when they joined
    private int totalTestsTaken;       // we'll populate this later from TestAttempts
    private double averageScore;       // we'll populate this later
}