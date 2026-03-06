package com.edutech.edutechbackend.entity;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "test_attempts")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TestAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;           // which student attempted

    @ManyToOne
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;               // which topic was tested

    private int score;                 // marks scored
    private int totalMarks;            // total possible marks
    private LocalDateTime attemptedAt;

    @OneToMany(mappedBy = "testAttempt", cascade = CascadeType.ALL)
    private List<AttemptAnswer> answers; // each answer the student gave

    @PrePersist
    public void prePersist() {
        this.attemptedAt = LocalDateTime.now();
    }
}
