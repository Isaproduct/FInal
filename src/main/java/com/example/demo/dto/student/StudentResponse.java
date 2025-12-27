package com.example.demo.dto.student;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class StudentResponse {
    private Long id;
    private String username;
    private String fullName;
    private String studentNo;
}
