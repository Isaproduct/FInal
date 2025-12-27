package com.example.demo.dto.course;

import com.example.demo.dto.teacher.TeacherShortResponse;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CourseResponse {
    private Long id;
    private String code;
    private String title;
    private TeacherShortResponse teacher;
}
