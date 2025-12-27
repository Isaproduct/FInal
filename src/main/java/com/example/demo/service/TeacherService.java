package com.example.demo.service;

import com.example.demo.dto.teacher.TeacherRequest;
import com.example.demo.dto.teacher.TeacherResponse;
import com.example.demo.mapper.TeacherMapper;
import com.example.demo.model.Role;
import com.example.demo.model.Teacher;
import com.example.demo.model.User;
import com.example.demo.repo.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final UserService userService;
    private final TeacherMapper teacherMapper;

    public List<TeacherResponse> findAll() {
        return teacherRepository.findAll().stream()
                .map(teacherMapper::toResponse)
                .toList();
    }

    public TeacherResponse findById(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found: " + id));
        return teacherMapper.toResponse(teacher);
    }

    public TeacherResponse create(TeacherRequest request) {
        User user = userService.createUser(request.getUsername(), request.getPassword(), Role.TEACHER);

        Teacher teacher = Teacher.builder()
                .user(user)
                .fullName(request.getFullName())
                .department(request.getDepartment())
                .build();

        return teacherMapper.toResponse(teacherRepository.save(teacher));
    }

    public TeacherResponse update(Long id, TeacherRequest request) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found: " + id));

        userService.updateUserCredentials(teacher.getUser().getId(), request.getUsername(), request.getPassword());

        teacher.setFullName(request.getFullName());
        teacher.setDepartment(request.getDepartment());

        return teacherMapper.toResponse(teacherRepository.save(teacher));
    }

    public void delete(Long id) {
        if (!teacherRepository.existsById(id)) {
            throw new IllegalArgumentException("Teacher not found: " + id);
        }
        teacherRepository.deleteById(id);
    }
}
