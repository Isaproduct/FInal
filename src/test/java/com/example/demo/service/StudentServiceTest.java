package com.example.demo.service;

import com.example.demo.dto.student.StudentRequest;
import com.example.demo.dto.student.StudentResponse;
import com.example.demo.mapper.StudentMapper;
import com.example.demo.model.Role;
import com.example.demo.model.Student;
import com.example.demo.model.User;
import com.example.demo.repo.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock private StudentRepository studentRepository;
    @Mock private UserService userService;
    @Mock private StudentMapper studentMapper;

    @InjectMocks private StudentService studentService;

    @Test
    void findAll_returnsMappedList() {
        Student s1 = Student.builder().id(1L).build();
        Student s2 = Student.builder().id(2L).build();

        when(studentRepository.findAll()).thenReturn(List.of(s1, s2));
        when(studentMapper.toResponse(s1)).thenReturn(StudentResponse.builder().id(1L).build());
        when(studentMapper.toResponse(s2)).thenReturn(StudentResponse.builder().id(2L).build());

        List<StudentResponse> res = studentService.findAll();

        assertEquals(2, res.size());
        assertEquals(1L, res.get(0).getId());
        assertEquals(2L, res.get(1).getId());
    }

    @Test
    void findById_notFound_throws() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> studentService.findById(99L));
    }

    @Test
    void create_duplicateStudentNo_throws() {
        StudentRequest req = StudentRequest.builder()
                .username("stud1")
                .password("pass")
                .fullName("Student One")
                .studentNo("S001")
                .build();

        when(studentRepository.existsByStudentNo("S001")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> studentService.create(req));

        verify(userService, never()).createUser(anyString(), anyString(), any(Role.class));
        verify(studentRepository, never()).save(any());
    }

    @Test
    void create_success_createsUserAndSavesStudent() {
        StudentRequest req = StudentRequest.builder()
                .username("stud1")
                .password("pass")
                .fullName("Student One")
                .studentNo("S001")
                .build();

        User createdUser = User.builder()
                .id(10L)
                .username("stud1")
                .role(Role.STUDENT)
                .enabled(true)
                .build();

        when(userService.createUser("stud1", "pass", Role.STUDENT)).thenReturn(createdUser);

        when(studentRepository.save(any(Student.class))).thenAnswer(inv -> {
            Student s = inv.getArgument(0);
            s.setId(5L);
            return s;
        });

        when(studentMapper.toResponse(any(Student.class))).thenAnswer(inv -> {
            Student s = inv.getArgument(0);
            return StudentResponse.builder()
                    .id(s.getId())
                    .username(s.getUser().getUsername())
                    .fullName(s.getFullName())
                    .studentNo(s.getStudentNo())
                    .build();
        });

        StudentResponse resp = studentService.create(req);

        assertEquals(5L, resp.getId());
        assertEquals("stud1", resp.getUsername());
        assertEquals("Student One", resp.getFullName());
        assertEquals("S001", resp.getStudentNo());

        ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(captor.capture());
        Student saved = captor.getValue();

        assertEquals(createdUser, saved.getUser());
        assertEquals("Student One", saved.getFullName());
        assertEquals("S001", saved.getStudentNo());
    }

    @Test
    void update_success_updatesCredentialsAndFields() {
        StudentRequest req = StudentRequest.builder()
                .username("newU")
                .password("newP")
                .fullName("New Name")
                .studentNo("S777")
                .build();

        User user = User.builder().id(10L).username("oldU").build();
        Student student = Student.builder()
                .id(1L)
                .user(user)
                .fullName("Old")
                .studentNo("S001")
                .build();

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));
        when(studentMapper.toResponse(any(Student.class))).thenReturn(StudentResponse.builder().id(1L).build());

        StudentResponse resp = studentService.update(1L, req);

        assertNotNull(resp);

        verify(userService).updateUserCredentials(10L, "newU", "newP");
        assertEquals("New Name", student.getFullName());
        assertEquals("S777", student.getStudentNo());
        verify(studentRepository).save(student);
    }

    @Test
    void delete_notFound_throws() {
        when(studentRepository.existsById(7L)).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> studentService.delete(7L));
        verify(studentRepository, never()).deleteById(anyLong());
    }

    @Test
    void delete_success_callsDelete() {
        when(studentRepository.existsById(7L)).thenReturn(true);
        studentService.delete(7L);
        verify(studentRepository).deleteById(7L);
    }
}
