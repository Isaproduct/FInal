package com.example.demo.dto.course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CourseRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String title;

    @NotNull
    private Long teacherId;
}
