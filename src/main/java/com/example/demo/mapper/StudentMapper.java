package com.example.demo.mapper;

import com.example.demo.dto.student.StudentResponse;
import com.example.demo.model.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StudentMapper {

    @Mapping(target = "username", source = "user.username")
    StudentResponse toResponse(Student student);
}
