package com.example.demo.mapper;

import com.example.demo.dto.teacher.TeacherResponse;
import com.example.demo.model.Teacher;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TeacherMapper {

    @Mapping(target = "username", source = "user.username")
    TeacherResponse toResponse(Teacher teacher);
}
