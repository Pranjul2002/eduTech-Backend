package com.edutech.edutechbackend.controller;


import com.edutech.edutechbackend.dto.StudentProfileDTO;
import com.edutech.edutechbackend.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/students")
// ↑ base URL for all student endpoints
//   /api/students/profile
//   /api/students/... (future endpoints)
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    // ↑ all business logic lives here
    //   controller stays thin — just receives and delegates

    // ════════════════════════════════════════════════════════════
    // GET PROFILE
    // GET /api/students/profile
    // PROTECTED — requires valid JWT token
    // ════════════════════════════════════════════════════════════
    @GetMapping("/profile")
    // ↑ maps HTTP GET /api/students/profile to this method
    //   GET because we're READING data, not creating/modifying
    public ResponseEntity<StudentProfileDTO> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        // ↑ @AuthenticationPrincipal
        //   Spring automatically injects the UserDetails object
        //   that JwtAuthFilter stored in SecurityContext
        //
        // Remember from JwtAuthFilter Step 3d:
        //   UsernamePasswordAuthenticationToken authToken =
        //     new UsernamePasswordAuthenticationToken(
        //       userDetails,   ← stored as principal HERE
        //       null,
        //       authorities
        //     );
        //   SecurityContextHolder.getContext().setAuthentication(authToken)
        //
        // @AuthenticationPrincipal reads that principal back out
        // userDetails.getUsername() → "john@gmail.com"
        // This is how we know WHO is making this request

        StudentProfileDTO profile = studentService.getProfile(
                userDetails.getUsername()
                // ↑ getUsername() returns email
                //   because in CustomUserDetailsService we did:
                //   new User(student.getEmail(), ...)
                //   getUsername() = student.getEmail()
        );

        return ResponseEntity.ok(profile);
        // ↑ 200 OK with StudentProfileDTO as JSON body
        //
        // Response:
        // {
        //   "id": 1,
        //   "name": "John Doe",
        //   "email": "john@gmail.com",
        //   "gender": "MALE",
        //   "dateOfBirth": "2000-05-15",
        //   "age": 23,
        //   "createdAt": "2024-03-01T10:30:00",
        //   "totalTestsTaken": 0,
        //   "averageScore": 0.0
        // }
    }
}