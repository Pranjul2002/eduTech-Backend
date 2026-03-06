package com.edutech.edutechbackend.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "attempt_answers")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AttemptAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "attempt_id", nullable = false)
    private TestAttempt testAttempt;   // which attempt this answer belongs to

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;         // which question was answered

    @ManyToOne
    @JoinColumn(name = "selected_option_id")
    private Option selectedOption;     // which option the student picked

    private boolean isCorrect;         // was their answer correct?
}