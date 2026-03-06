package com.edutech.edutechbackend.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

@Entity
@Table(name = "students")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;           // will be stored encrypted (bcrypt)

    // ── FIELD: Gender ───────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    // ↑ tells JPA: store enum as STRING in DB ("MALE", "FEMALE", "OTHER")
    //   NOT as number (0, 1, 2) — EnumType.ORDINAL would do that
    //   STRING is better because:
    //     → DB is readable: "MALE" vs "0"
    //     → adding new enum values won't break existing data
    //     → if you reorder enum values, ORDINAL breaks, STRING doesn't
    @Column(nullable = false)
    private Gender gender;

    // ── FIELD: Date of Birth ────────────────────────────────────────
    @Column(nullable = false)
    private LocalDate dateOfBirth;
    // ↑ LocalDate = date only (no time component)
    //   e.g. 2000-05-15
    //   stored as DATE type in PostgreSQL
    //   we calculate age from this dynamically


    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist                        // runs automatically before saving to DB
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // ── CALCULATED FIELD: Age ─────────────────────────────────────────────
    // NOT stored in DB — calculated dynamically when needed
    // @Transient tells JPA: don't map this to any DB column
    @Transient
    public int getAge() {
        if (this.dateOfBirth == null) return 0;

        return Period.between(this.dateOfBirth, LocalDate.now()).getYears();
        // Period.between(start, end).getYears()
        // calculates exact difference in years
        // e.g. born 2000-05-15, today 2024-03-01
        //      Period = 23 years, 9 months, 14 days
        //      getYears() = 23
        //
        // automatically correct every day
        // no manual updates needed ever
    }
}
