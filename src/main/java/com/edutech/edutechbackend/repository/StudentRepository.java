package com.edutech.edutechbackend.repository;


import com.edutech.edutechbackend.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    // Spring Data JPA auto-implements this from the method name
    // translates to: SELECT * FROM students WHERE email = ?
    Optional<Student> findByEmail(String email);

    // we'll also need this during registration to check duplicate emails
    boolean existsByEmail(String email);
}
