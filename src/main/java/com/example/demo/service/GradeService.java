package com.example.demo.service;

import com.example.demo.dto.grade.GradeRequest;
import com.example.demo.dto.grade.GradeResponse;
import com.example.demo.mapper.GradeMapper;
import com.example.demo.model.Course;
import com.example.demo.model.Grade;
import com.example.demo.model.Student;
import com.example.demo.repo.CourseRepository;
import com.example.demo.repo.GradeRepository;
import com.example.demo.repo.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GradeService {
    private final GradeRepository gradeRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final GradeMapper gradeMapper;

    public List<GradeResponse> findAll() {
        return gradeRepository.findAll().stream()
                .map(gradeMapper::toResponse)
                .toList();
    }

    public List<GradeResponse> findByStudent(Long studentId) {
        return gradeRepository.findByStudentId(studentId).stream()
                .map(gradeMapper::toResponse)
                .toList();
    }

    // ✅ Мои оценки по username из JWT
    public List<GradeResponse> findMy(String username) {
        Student student = studentRepository.findByUserUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Student profile not found for user: " + username
                ));

        return findByStudent(student.getId());
    }

    public GradeResponse create(GradeRequest request) {
        if (gradeRepository.existsByStudentIdAndCourseId(request.getStudentId(), request.getCourseId())) {
            throw new IllegalArgumentException("Grade already exists for studentId=" + request.getStudentId() +
                    " and courseId=" + request.getCourseId());
        }

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + request.getStudentId()));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + request.getCourseId()));

        Grade grade = Grade.builder()
                .student(student)
                .course(course)
                .value(request.getValue())
                .comment(request.getComment())
                .build();

        return gradeMapper.toResponse(gradeRepository.save(grade));
    }

    public void delete(Long id) {
        gradeRepository.deleteById(id);
    }
}
