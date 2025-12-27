package com.example.demo.mapper;

import com.example.demo.dto.grade.GradeResponse;
import com.example.demo.model.Grade;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GradeMapper {

    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "courseId", source = "course.id")
    GradeResponse toResponse(Grade grade);
}
