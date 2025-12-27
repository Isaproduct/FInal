package com.example.demo.dto.teacher;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TeacherShortResponse {
    private Long id;
    private String fullName;
}
