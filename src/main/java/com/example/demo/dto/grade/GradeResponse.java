package com.example.demo.dto.grade;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class GradeResponse {
    private Long id;
    private Long studentId;
    private Long courseId;
    private Integer value;
    private String comment;
}
