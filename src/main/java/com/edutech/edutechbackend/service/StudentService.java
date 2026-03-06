package com.edutech.edutechbackend.service;


import com.edutech.edutechbackend.dto.StudentProfileDTO;
import com.edutech.edutechbackend.entity.Student;
import com.edutech.edutechbackend.exception.StudentNotFoundException;
import com.edutech.edutechbackend.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    // ↑ to fetch student from DB by email

    // ════════════════════════════════════════════════════════════
    // GET PROFILE
    // ════════════════════════════════════════════════════════════
    public StudentProfileDTO getProfile(String email) {
        // ↑ email comes from SecurityContext via controller
        //   it's the email we extracted from JWT token
        //   guaranteed to be the currently logged-in student

        // ── STEP 1: Find student in DB ─────────────────────────────────────
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new StudentNotFoundException(
                        "Student not found with email: " + email
                ));
        // ↑ findByEmail() → SELECT * FROM students WHERE email = ?
        //
        // This student DEFINITELY exists because:
        //   → JwtAuthFilter already validated their token
        //   → CustomUserDetailsService already loaded them from DB
        //   → if they didn't exist, request would have been rejected already
        //
        // orElseThrow() is a safety net for edge cases like:
        //   → student account deleted between token issue and this request
        //   → data inconsistency

        // ── STEP 2: Build and return StudentProfileDTO ────────────────────
        return StudentProfileDTO.builder()
                .id(student.getId())
                // ↑ 1

                .name(student.getName())
                // ↑ "John Doe"

                .email(student.getEmail())
                // ↑ "john@gmail.com"

                .gender(student.getGender())
                // ↑ Gender.MALE → serialized as "MALE" in JSON

                .dateOfBirth(student.getDateOfBirth())
                // ↑ 2000-05-15

                .age(student.getAge())
                // ↑ calls @Transient getAge() on Student entity
                //   Period.between(dateOfBirth, today).getYears()
                //   → 23 (calculated dynamically, always accurate)

                .createdAt(student.getCreatedAt())
                // ↑ when they registered: 2024-03-01T10:30:00

                .totalTestsTaken(0)
                // ↑ placeholder for now
                //   will be updated in Step 5 (Progress Tracking)
                //   when we count TestAttempt records for this student

                .averageScore(0.0)
                // ↑ placeholder for now
                //   will be updated in Step 5
                //   when we calculate average score from TestAttempts

                .build();
    }
}