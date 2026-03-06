package com.edutech.edutechbackend.entity;


import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "topics")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;               // e.g. "Algebra"

    private String description;

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;           // many topics belong to one subject

    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL)
    private List<Question> questions;  // one topic has many questions
}
