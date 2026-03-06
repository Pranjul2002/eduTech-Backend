package com.edutech.edutechbackend.entity;


import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "questions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;       // the actual question

    @ManyToOne
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;               // which topic this question belongs to

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    private List<Option> options;      // 4 options for this question
}
