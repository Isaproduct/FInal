package com.example.demo.service;

import com.example.demo.dto.course.CourseRequest;
import com.example.demo.dto.course.CourseResponse;
import com.example.demo.mapper.CourseMapper;
import com.example.demo.model.Course;
import com.example.demo.model.Teacher;
import com.example.demo.repo.CourseRepository;
import com.example.demo.repo.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;
    private final CourseMapper courseMapper;

    public List<CourseResponse> findAll() {
        return courseRepository.findAll().stream()
                .map(courseMapper::toResponse)
                .toList();
    }

    public CourseResponse findById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + id));
        return courseMapper.toResponse(course);
    }

    public CourseResponse create(CourseRequest request) {
        if (courseRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Course code already exists: " + request.getCode());
        }

        Teacher teacher = teacherRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found: " + request.getTeacherId()));

        Course course = Course.builder()
                .code(request.getCode())
                .title(request.getTitle())
                .teacher(teacher)
                .build();

        return courseMapper.toResponse(courseRepository.save(course));
    }

    public CourseResponse update(Long id, CourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + id));

        // уникальность code при изменении
        if (!course.getCode().equals(request.getCode())) {
            courseRepository.findByCode(request.getCode()).ifPresent(other -> {
                if (!other.getId().equals(id)) {
                    throw new IllegalArgumentException("Course code already exists: " + request.getCode());
                }
            });
        }

        Teacher teacher = teacherRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found: " + request.getTeacherId()));

        course.setCode(request.getCode());
        course.setTitle(request.getTitle());
        course.setTeacher(teacher);

        return courseMapper.toResponse(courseRepository.save(course));
    }

    public void delete(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new IllegalArgumentException("Course not found: " + id);
        }
        courseRepository.deleteById(id);
    }
}
