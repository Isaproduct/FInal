package com.example.demo.service;

import com.example.demo.dto.teacher.TeacherRequest;
import com.example.demo.dto.teacher.TeacherResponse;
import com.example.demo.mapper.TeacherMapper;
import com.example.demo.model.Role;
import com.example.demo.model.Teacher;
import com.example.demo.model.User;
import com.example.demo.repo.TeacherRepository;
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
class TeacherServiceTest {

    @Mock private TeacherRepository teacherRepository;
    @Mock private UserService userService;
    @Mock private TeacherMapper teacherMapper;

    @InjectMocks private TeacherService teacherService;

    @Test
    void findAll_returnsMappedList() {
        Teacher t1 = Teacher.builder().id(1L).build();
        Teacher t2 = Teacher.builder().id(2L).build();

        when(teacherRepository.findAll()).thenReturn(List.of(t1, t2));
        when(teacherMapper.toResponse(t1)).thenReturn(TeacherResponse.builder().id(1L).build());
        when(teacherMapper.toResponse(t2)).thenReturn(TeacherResponse.builder().id(2L).build());

        List<TeacherResponse> res = teacherService.findAll();

        assertEquals(2, res.size());
        assertEquals(1L, res.get(0).getId());
        assertEquals(2L, res.get(1).getId());
    }

    @Test
    void findById_notFound_throws() {
        when(teacherRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> teacherService.findById(99L));
    }

    @Test
    void create_success_createsUserAndSavesTeacher() {
        TeacherRequest req = TeacherRequest.builder()
                .username("teach1")
                .password("pass")
                .fullName("Teacher One")
                .department("CS")
                .build();

        User createdUser = User.builder().id(10L).username("teach1").role(Role.TEACHER).enabled(true).build();

        when(userService.createUser("teach1", "pass", Role.TEACHER)).thenReturn(createdUser);
        when(teacherRepository.save(any(Teacher.class))).thenAnswer(inv -> {
            Teacher t = inv.getArgument(0);
            t.setId(5L);
            return t;
        });

        when(teacherMapper.toResponse(any(Teacher.class))).thenAnswer(inv -> {
            Teacher t = inv.getArgument(0);
            return TeacherResponse.builder()
                    .id(t.getId())
                    .username(t.getUser().getUsername())
                    .fullName(t.getFullName())
                    .department(t.getDepartment())
                    .build();
        });

        TeacherResponse resp = teacherService.create(req);

        assertEquals(5L, resp.getId());
        assertEquals("teach1", resp.getUsername());
        assertEquals("Teacher One", resp.getFullName());
        assertEquals("CS", resp.getDepartment());

        ArgumentCaptor<Teacher> captor = ArgumentCaptor.forClass(Teacher.class);
        verify(teacherRepository).save(captor.capture());
        Teacher saved = captor.getValue();
        assertEquals(createdUser, saved.getUser());
        assertEquals("Teacher One", saved.getFullName());
        assertEquals("CS", saved.getDepartment());
    }

    @Test
    void update_success_updatesCredentialsAndFields() {
        TeacherRequest req = TeacherRequest.builder()
                .username("newU")
                .password("newP")
                .fullName("New Name")
                .department("New Dep")
                .build();

        User user = User.builder().id(10L).username("oldU").build();
        Teacher teacher = Teacher.builder().id(1L).user(user).fullName("Old").department("Old").build();

        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(teacherRepository.save(any(Teacher.class))).thenAnswer(inv -> inv.getArgument(0));
        when(teacherMapper.toResponse(any(Teacher.class))).thenReturn(TeacherResponse.builder().id(1L).build());

        TeacherResponse resp = teacherService.update(1L, req);

        assertNotNull(resp);

        verify(userService).updateUserCredentials(10L, "newU", "newP");
        assertEquals("New Name", teacher.getFullName());
        assertEquals("New Dep", teacher.getDepartment());
        verify(teacherRepository).save(teacher);
    }

    @Test
    void delete_notFound_throws() {
        when(teacherRepository.existsById(7L)).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> teacherService.delete(7L));
        verify(teacherRepository, never()).deleteById(anyLong());
    }

    @Test
    void delete_success_callsDelete() {
        when(teacherRepository.existsById(7L)).thenReturn(true);
        teacherService.delete(7L);
        verify(teacherRepository).deleteById(7L);
    }
}
