package com.edutech.edutechbackend.service;


import com.edutech.edutechbackend.entity.Student;
import com.edutech.edutechbackend.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    // ── we need StudentRepository to hit the database ──────────────────────
    private final StudentRepository studentRepository;

    // ── THE bridge method ───────────────────────────────────────────────────
    // Spring Security calls this whenever it needs to know about a user
    // Called in TWO places (as we discussed):
    //   1. JWT Filter → to validate token on every protected request
    //   2. Login → to verify password during authentication
    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        // Step 1 → go to YOUR database and find the student by email
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() ->
                        // if not found → throw this exception
                        // Spring Security catches this and returns 401
                        new UsernameNotFoundException(
                                "Student not found with email: " + email
                        )
                );

        // Step 2 → convert Student (your world) → UserDetails (Spring's world)
        // Using Way 1 from our discussion → Spring's built-in User class
        // new User(username,    password,          authorities)
        return new User(student.getEmail(), student.getPassword(), new ArrayList<>());
        //              ↑                   ↑                       ↑
        //         getUsername()      getPassword()         empty for now
        //         returns this       returns this          no roles yet
        //         when called        when called           we'll add ROLE_STUDENT
        //                                                  later
    }
}
