package com.example.demo.service;

import com.example.demo.dto.student.StudentRequest;
import com.example.demo.dto.student.StudentResponse;
import com.example.demo.mapper.StudentMapper;
import com.example.demo.model.Role;
import com.example.demo.model.Student;
import com.example.demo.model.User;
import com.example.demo.repo.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserService userService;
    private final StudentMapper studentMapper;

    public List<StudentResponse> findAll() {
        return studentRepository.findAll().stream()
                .map(studentMapper::toResponse)
                .toList();
    }

    public StudentResponse findById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + id));
        return studentMapper.toResponse(student);
    }

    public StudentResponse create(StudentRequest request) {
        if (studentRepository.existsByStudentNo(request.getStudentNo())) {
            throw new IllegalArgumentException("StudentNo already exists: " + request.getStudentNo());
        }

        User user = userService.createUser(request.getUsername(), request.getPassword(), Role.STUDENT);

        Student student = Student.builder()
                .user(user)
                .fullName(request.getFullName())
                .studentNo(request.getStudentNo())
                .build();

        return studentMapper.toResponse(studentRepository.save(student));
    }

    public StudentResponse update(Long id, StudentRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + id));

        if (!student.getStudentNo().equals(request.getStudentNo())
                && studentRepository.existsByStudentNo(request.getStudentNo())) {
            throw new IllegalArgumentException("StudentNo already exists: " + request.getStudentNo());
        }

        userService.updateUserCredentials(student.getUser().getId(), request.getUsername(), request.getPassword());

        student.setFullName(request.getFullName());
        student.setStudentNo(request.getStudentNo());

        return studentMapper.toResponse(studentRepository.save(student));
    }

    public void delete(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new IllegalArgumentException("Student not found: " + id);
        }
        studentRepository.deleteById(id);
    }
}
