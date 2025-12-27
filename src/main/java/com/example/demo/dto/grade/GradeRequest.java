package com.example.demo.dto.grade;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class GradeRequest {

    @NotNull
    private Long studentId;

    @NotNull
    private Long courseId;

    @NotNull
    @Min(0)
    @Max(100)
    private Integer value;

    private String comment;
}
