package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "grades",
        uniqueConstraints = @UniqueConstraint(name = "uk_grade_student_course", columnNames = {"student_id", "course_id"})
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private Integer value; // например 0..100

    @Column(length = 255)
    private String comment;
}
