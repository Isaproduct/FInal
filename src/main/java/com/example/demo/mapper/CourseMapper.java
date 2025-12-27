package com.example.demo.mapper;

import com.example.demo.dto.course.CourseResponse;
import com.example.demo.dto.teacher.TeacherShortResponse;
import com.example.demo.model.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CourseMapper {

    @Mapping(target = "teacher", expression = "java(toTeacherShortResponse(course))")
    CourseResponse toResponse(Course course);

    default TeacherShortResponse toTeacherShortResponse(Course course) {
        if (course == null || course.getTeacher() == null) return null;
        return TeacherShortResponse.builder()
                .id(course.getTeacher().getId())
                .fullName(course.getTeacher().getFullName())
                .build();
    }
}
