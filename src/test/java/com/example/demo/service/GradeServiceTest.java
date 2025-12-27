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
class GradeServiceTest {

    @Mock private GradeRepository gradeRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private GradeMapper gradeMapper;

    @InjectMocks private GradeService gradeService;

    @Test
    void findAll_returnsMapped() {
        Grade g1 = Grade.builder().id(1L).value(80).build();
        Grade g2 = Grade.builder().id(2L).value(90).build();

        when(gradeRepository.findAll()).thenReturn(List.of(g1, g2));
        when(gradeMapper.toResponse(g1)).thenReturn(GradeResponse.builder().id(1L).value(80).build());
        when(gradeMapper.toResponse(g2)).thenReturn(GradeResponse.builder().id(2L).value(90).build());

        List<GradeResponse> res = gradeService.findAll();

        assertEquals(2, res.size());
        assertEquals(80, res.get(0).getValue());
        assertEquals(90, res.get(1).getValue());
    }

    @Test
    void findByStudent_returnsMapped() {
        Grade g1 = Grade.builder().id(1L).value(70).build();

        when(gradeRepository.findByStudentId(5L)).thenReturn(List.of(g1));
        when(gradeMapper.toResponse(g1)).thenReturn(GradeResponse.builder().id(1L).value(70).build());

        List<GradeResponse> res = gradeService.findByStudent(5L);

        assertEquals(1, res.size());
        assertEquals(70, res.get(0).getValue());
    }

    @Test
    void findMy_studentProfileNotFound_throws() {
        when(studentRepository.findByUserUsername("u")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> gradeService.findMy("u"));
    }

    @Test
    void findMy_success_usesStudentId() {
        Student s = Student.builder().id(10L).build();
        Grade g = Grade.builder().id(1L).value(88).build();

        when(studentRepository.findByUserUsername("u")).thenReturn(Optional.of(s));
        when(gradeRepository.findByStudentId(10L)).thenReturn(List.of(g));
        when(gradeMapper.toResponse(g)).thenReturn(GradeResponse.builder().id(1L).value(88).build());

        List<GradeResponse> res = gradeService.findMy("u");

        assertEquals(1, res.size());
        assertEquals(88, res.get(0).getValue());
        verify(gradeRepository).findByStudentId(10L);
    }

    @Test
    void create_duplicateGrade_throws() {
        GradeRequest req = GradeRequest.builder().studentId(1L).courseId(2L).value(90).comment("ok").build();
        when(gradeRepository.existsByStudentIdAndCourseId(1L, 2L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> gradeService.create(req));
        verify(gradeRepository, never()).save(any());
    }

    @Test
    void create_success_savesGrade() {
        GradeRequest req = GradeRequest.builder().studentId(1L).courseId(2L).value(95).comment("good").build();

        Student s = Student.builder().id(1L).build();
        Course c = Course.builder().id(2L).build();

        when(gradeRepository.existsByStudentIdAndCourseId(1L, 2L)).thenReturn(false);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(s));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(c));

        when(gradeRepository.save(any(Grade.class))).thenAnswer(inv -> {
            Grade g = inv.getArgument(0);
            g.setId(10L);
            return g;
        });

        when(gradeMapper.toResponse(any(Grade.class))).thenAnswer(inv -> {
            Grade g = inv.getArgument(0);
            return GradeResponse.builder().id(g.getId()).value(g.getValue()).comment(g.getComment()).build();
        });

        GradeResponse resp = gradeService.create(req);

        assertEquals(10L, resp.getId());
        assertEquals(95, resp.getValue());
        assertEquals("good", resp.getComment());
        verify(gradeRepository).save(any(Grade.class));
    }

    @Test
    void delete_callsRepositoryDelete() {
        gradeService.delete(7L);
        verify(gradeRepository).deleteById(7L);
    }
}

