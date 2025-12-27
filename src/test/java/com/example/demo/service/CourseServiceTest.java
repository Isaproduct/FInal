package com.example.demo.service;

import com.example.demo.dto.course.CourseRequest;
import com.example.demo.dto.course.CourseResponse;
import com.example.demo.mapper.CourseMapper;
import com.example.demo.model.Course;
import com.example.demo.model.Teacher;
import com.example.demo.repo.CourseRepository;
import com.example.demo.repo.TeacherRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock private CourseRepository courseRepository;
    @Mock private TeacherRepository teacherRepository;
    @Mock private CourseMapper courseMapper;

    @InjectMocks private CourseService courseService;

    @Test
    void findAll_returnsMappedList() {
        Course c1 = Course.builder().id(1L).code("A").title("T1").build();
        Course c2 = Course.builder().id(2L).code("B").title("T2").build();

        when(courseRepository.findAll()).thenReturn(List.of(c1, c2));
        when(courseMapper.toResponse(c1)).thenReturn(CourseResponse.builder().id(1L).code("A").build());
        when(courseMapper.toResponse(c2)).thenReturn(CourseResponse.builder().id(2L).code("B").build());

        List<CourseResponse> res = courseService.findAll();

        assertEquals(2, res.size());
        assertEquals("A", res.get(0).getCode());
        assertEquals("B", res.get(1).getCode());
    }

    @Test
    void findById_notFound_throws() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> courseService.findById(99L));
    }

    @Test
    void create_duplicateCode_throws() {
        CourseRequest req = CourseRequest.builder().code("CS101").title("Intro").teacherId(1L).build();
        when(courseRepository.existsByCode("CS101")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> courseService.create(req));
        verify(courseRepository, never()).save(any());
    }

    @Test
    void create_success_savesCourse() {
        CourseRequest req = CourseRequest.builder().code("CS101").title("Intro").teacherId(7L).build();
        Teacher t = Teacher.builder().id(7L).build();

        when(courseRepository.existsByCode("CS101")).thenReturn(false);
        when(teacherRepository.findById(7L)).thenReturn(Optional.of(t));

        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> {
            Course c = inv.getArgument(0);
            c.setId(10L);
            return c;
        });

        when(courseMapper.toResponse(any(Course.class))).thenAnswer(inv -> {
            Course c = inv.getArgument(0);
            return CourseResponse.builder().id(c.getId()).code(c.getCode()).title(c.getTitle()).build();
        });

        CourseResponse resp = courseService.create(req);

        assertEquals(10L, resp.getId());
        assertEquals("CS101", resp.getCode());
        assertEquals("Intro", resp.getTitle());
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void update_codeAlreadyExistsForAnotherCourse_throws() {
        Course existing = Course.builder().id(1L).code("OLD").title("X").teacher(Teacher.builder().id(1L).build()).build();
        Course other = Course.builder().id(2L).code("NEW").build();

        CourseRequest req = CourseRequest.builder().code("NEW").title("Y").teacherId(1L).build();

        when(courseRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(courseRepository.findByCode("NEW")).thenReturn(Optional.of(other));

        assertThrows(IllegalArgumentException.class, () -> courseService.update(1L, req));
        verify(courseRepository, never()).save(any());
    }

    @Test
    void delete_notFound_throws() {
        when(courseRepository.existsById(5L)).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> courseService.delete(5L));
    }

    @Test
    void delete_success_callsDelete() {
        when(courseRepository.existsById(5L)).thenReturn(true);
        courseService.delete(5L);
        verify(courseRepository).deleteById(5L);
    }
}
