package com.example.demo.dto.teacher;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TeacherResponse {
    private Long id;
    private String username;
    private String fullName;
    private String department;
}
