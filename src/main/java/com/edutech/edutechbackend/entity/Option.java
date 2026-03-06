package com.edutech.edutechbackend.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "options")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Option {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String optionText;         // the answer choice text

    @Column(nullable = false)
    private boolean isCorrect;         // true for the correct answer

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;         // which question this option belongs to
}
